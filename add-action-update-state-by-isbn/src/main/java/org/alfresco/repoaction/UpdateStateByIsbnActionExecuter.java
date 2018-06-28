package org.alfresco.repoaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author otves
 */
public class UpdateStateByIsbnActionExecuter extends ActionExecuterAbstractBase {

    private static Log LOGGER = LogFactory.getLog(UpdateStateByIsbnActionExecuter.class);

    private static final String ISBN = "isbn";

    private static final String AVAILABLE  = "доступен";

    private static final String UNAVAILABLE = "недоступен";

    /**
     * The Alfresco Service Registry that gives access to all public content services in Alfresco.
     */
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        if (serviceRegistry.getNodeService().exists(actionedUponNodeRef)) {
            Map<QName, Serializable>  params = serviceRegistry.getNodeService().getProperties(actionedUponNodeRef);
            String isbn = null;
            for(Map.Entry<QName, Serializable> param : params.entrySet()) {
                if(ISBN.equalsIgnoreCase(param.getKey().getLocalName())) {
                    isbn = (String) param.getValue();
                    break;
                }
            }

            if(isbn == null) {
                LOGGER.error("ISBN is not set");
                return;
            }

            try {
                TaggingService taggingService = serviceRegistry.getTaggingService();
                taggingService.clearTags(actionedUponNodeRef);
                if(isDocumentAvailable(isbn)) {
                    SimpleDateFormat dt1 = new SimpleDateFormat("dd.mm.yyyy");
                    String nowStrDate = dt1.format(new Date());
                    taggingService.addTag(actionedUponNodeRef, "обновлено " + nowStrDate);
                    taggingService.addTag(actionedUponNodeRef, AVAILABLE);
                } else {
                    taggingService.addTag(actionedUponNodeRef, UNAVAILABLE);
                }
            } catch (Exception e) {
                LOGGER.error("", e);
                throw new AlfrescoRuntimeException("Could get isbn data");
            }
        }
    }

    private boolean isDocumentAvailable(String isbn) throws Exception {
        Document xml = getIsbnResponse(isbn);
        NodeList items = xml.getElementsByTagName("Items");
        for(int i = 0; i < items.getLength(); i ++) {
           String text = items.item(i).getTextContent().replaceAll("[ |\n]{1,}", "").toLowerCase();
           if(text.startsWith("available")) {
               return Integer.valueOf(text.substring(9)) > 0;
           }
        }
        return false;
    }

    private Document getIsbnResponse(String isbn) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://193.233.14.18:210/svrgo?version=1.1&operation=searchRetrieve&query=dc.Identifier=0-19-8%24&maximumRecords=1&recordSchema=user");
        httpGet.addHeader("User-Agent", "");
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        LOGGER.debug("GET Response Status:: " + httpResponse.getStatusLine().getStatusCode());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(httpResponse.getEntity().getContent());
        return doc;
    }
}
