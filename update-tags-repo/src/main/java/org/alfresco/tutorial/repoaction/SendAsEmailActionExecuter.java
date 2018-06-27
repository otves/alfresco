/**
 * Copyright (C) 2015 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK Samples project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.alfresco.tutorial.repoaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Alfresco Repository Action that can send emails with file attachments.
 *
 * @author martin.bergljung@alfresco.com
 */
public class SendAsEmailActionExecuter extends ActionExecuterAbstractBase {
    private static Log logger = LogFactory.getLog(SendAsEmailActionExecuter.class);

    public static final String PARAM_EMAIL_TO_NAME = "to";
    public static final String PARAM_EMAIL_SUBJECT_NAME = "subject";
    public static final String PARAM_EMAIL_BODY_NAME = "body_text";

    /**
     * The Alfresco Service Registry that gives access to all public content services in Alfresco.
     */
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        for (String s : new String[]{PARAM_EMAIL_TO_NAME, PARAM_EMAIL_SUBJECT_NAME, PARAM_EMAIL_BODY_NAME}) {
            paramList.add(new ParameterDefinitionImpl(s, DataTypeDefinition.TEXT, true, getParamDisplayLabel(s)));
        }
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        if (serviceRegistry.getNodeService().exists(actionedUponNodeRef) == true) {


            // Get the email properties entered via Share Form
            String to = (String) action.getParameterValue(PARAM_EMAIL_TO_NAME);
            String subject = (String) action.getParameterValue(PARAM_EMAIL_SUBJECT_NAME);
            String body = (String) action.getParameterValue(PARAM_EMAIL_BODY_NAME);

            // Get document filename
            Serializable filename = serviceRegistry.getNodeService().getProperty(
                    actionedUponNodeRef, ContentModel.PROP_NAME);
            if (filename == null) {
                throw new AlfrescoRuntimeException("Document filename is null");
            }

            try {


                serviceRegistry.getNodeService().addAspect(actionedUponNodeRef, ContentModel.ASPECT_EMAILED, properties);
            } catch (MessagingException me) {
                me.printStackTrace();
                throw new AlfrescoRuntimeException("Could not send email: " + me.getMessage());
            }
        }
    }


}
