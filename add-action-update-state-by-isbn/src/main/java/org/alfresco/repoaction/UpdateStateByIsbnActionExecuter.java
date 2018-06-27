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

package org.alfresco.repoaction;

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
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.*;

import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.util.*;

/**
 *
 * @author otves
 */
public class UpdateStateByIsbnActionExecuter extends ActionExecuterAbstractBase {

    private static Log logger = LogFactory.getLog(UpdateStateByIsbnActionExecuter.class);

    static {
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>UpdateStateByIsbnActionExecuter!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public static final String PARAM_ISBN = "isbn";

    /**
     * The Alfresco Service Registry that gives access to all public content services in Alfresco.
     */
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        for (String s : new String[]{PARAM_ISBN}) {
            paramList.add(new ParameterDefinitionImpl(s, DataTypeDefinition.TEXT, true, getParamDisplayLabel(s)));
        }
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        logger.info(">>>>>>>>>>>>>>>>>>> UpdateStateByIsbnActionExecuter.executeImpl " + actionedUponNodeRef);

        if (serviceRegistry.getNodeService().exists(actionedUponNodeRef) == true) {
            Map<QName, Serializable>  params = serviceRegistry.getNodeService().getProperties(actionedUponNodeRef);

            for(Map.Entry<QName, Serializable> param : params.entrySet()) {
                logger.info(">>p:" + param.getKey() + " = " + param.getValue());
            }

            // Get properties entered via Share Form
            String isbn = (String) action.getParameterValue(PARAM_ISBN);
            serviceRegistry.getTaggingService().addTag(actionedUponNodeRef, "updated:!!!!!");


//            try {
//                final boolean isAvailaiable = isAvailaiable(isbn);
//            } catch (Exception e) {
//                logger.error("", e);
//                throw new AlfrescoRuntimeException("Could get isbn data");
//            }
//
//            // Get document filename
//            Serializable filename = serviceRegistry.getNodeService().getProperty(
//                    actionedUponNodeRef, ContentModel.PROP_NAME);
//            if (filename == null) {
//                throw new AlfrescoRuntimeException("Document filename is null");
//            }
//            String documentName = (String) filename;
//
//            try {
//
////                Properties  mailServerProperties = System.getProperties();
////                mailServerProperties.put("mail.smtp.host", "localhost");
////                mailServerProperties.put("mail.smtp.port", "2525");
//
//            } catch (MessagingException me) {
//                me.printStackTrace();
//                throw new AlfrescoRuntimeException("Could not send email: " + me.getMessage());
//            }
        }
    }

    //todo:
    private boolean isAvailaiable(String isbn) throws Exception {
        String xml = getIsbnResponse(isbn);
        return true;
    }

    private String getIsbnResponse(String isbn) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://193.233.14.18:210/svrgo?version=1.1&operation=searchRetrieve&query=dc.Identifier=0-19-8%24&maximumRecords=1&recordSchema=user");
        httpGet.addHeader("User-Agent", "");
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        logger.debug("GET Response Status:: " + httpResponse.getStatusLine().getStatusCode());
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();
        logger.debug("GET Response body:: " + response.toString());
        httpClient.close();
        return response.toString();
    }
}
