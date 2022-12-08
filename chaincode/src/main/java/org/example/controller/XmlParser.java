package org.example.controller;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.example.model.rulesetdatamodels.DecisionAsset;
import org.example.model.rulesetdatamodels.Input;
import org.example.model.rulesetdatamodels.Output;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XmlParser {
    static class OrganizationAssociation{
        String columnName;
        String organizationMspid;
        String textAnnotationId;
        String dataId;
    }


    public static String recoverStringParameterFromBytes(byte[] parameter){
        char c1 = 39; // ' character
        char c2 = 34; // " character
        String sortedJson = new String(parameter, StandardCharsets.UTF_8);
        return sortedJson.replace(c1,c2);
    }

    public static Document loadXMLFromString(String xml) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public static DecisionAsset createDecisionAssetFromXml(String xml) throws ParserConfigurationException, SAXException, IOException{
        Document document = loadXMLFromString(xml);
        System.out.println(document);
        Node node = document.getElementsByTagName("decision").item(0);
        Element decisionElement = (Element) node;
        List<Input> inputs = new ArrayList<Input>();
        List<Output> outputs = new ArrayList<Output>();

        //get meta information from decision
        String decisionName = decisionElement.getAttribute("name");
        Element tableElement = (Element) decisionElement.getElementsByTagName("decisionTable").item(0);
        String decisionId = tableElement.getAttribute("id");;
        String hitPolicy = tableElement.getAttribute("hitPolicy");

        //get inputs
        NodeList inputColumns = tableElement.getElementsByTagName("input");
        for (int i = 0; i < inputColumns.getLength(); i++) {
            Element inputElement = (Element) inputColumns.item(i);
            Element inputExpressionElement = (Element) inputElement.getElementsByTagName("inputExpression").item(0);
            Element inputValuesElement = (Element) inputElement.getElementsByTagName("inputValues").item(0);
            String[] defaultValues = new String[0];
            if (inputValuesElement != null){
                defaultValues = inputValuesElement.getElementsByTagName("text").item(0).getTextContent().split(",");
            }
            Input input = new Input(inputElement.getAttribute("id"), inputElement.getAttribute("label"),inputExpressionElement.getAttribute("typeRef") , Arrays.asList(defaultValues));
            inputs.add(input);
        }
        //get outputs
        NodeList outputColumns = tableElement.getElementsByTagName("output");
        for (int i = 0; i < outputColumns.getLength(); i++) {
            Element outputElement = (Element) outputColumns.item(i);
            Output output = new Output(outputElement.getAttribute("id"), outputElement.getAttribute("label"),outputElement.getAttribute("typeRef"));
            outputs.add(output);
        }

        //get rules
        NodeList rules = tableElement.getElementsByTagName("rule");
        for (int i = 0; i < rules.getLength(); i++) {
            Element ruleElement = (Element) rules.item(i);
            NodeList inputEntries = ruleElement.getElementsByTagName("inputEntry");
            for (int j = 0; j < inputEntries.getLength(); j++){
                Element inputEntryElement = (Element) inputEntries.item(j);
                String expression  = inputEntryElement.getElementsByTagName("text").item(0).getTextContent();
                if (expression == null)
                    expression = "";
                inputs.get(j).values.add(expression);
            }
            NodeList outputEntries = ruleElement.getElementsByTagName("outputEntry");
            for (int j = 0; j < outputEntries.getLength(); j++){
                Element outputEntryElement = (Element) outputEntries.item(j);
                String expression  = outputEntryElement.getElementsByTagName("text").item(0).getTextContent();
                if (expression == null)
                    expression = "";
                outputs.get(j).values.add(expression);
            }
        }

        //create the relationships between the columns and organizations

        List<OrganizationAssociation> orgAssociations = new ArrayList<>();
        //get inputData
        NodeList inputData = document.getElementsByTagName("inputData");
        for (int i = 0; i < inputData.getLength(); i++){
            Element inputDataElement = (Element) inputData.item(i);
            OrganizationAssociation orgAssociation = new OrganizationAssociation();
            orgAssociation.dataId = inputDataElement.getAttribute("id");
            orgAssociation.columnName = inputDataElement.getAttribute("name");
            orgAssociations.add(orgAssociation);
        }

        //get outputData
        NodeList outputData = document.getElementsByTagName("knowledgeSource");
        for (int i = 0; i < outputData.getLength(); i++){
            Element outputDataElement = (Element) outputData.item(i);
            OrganizationAssociation orgAssociation = new OrganizationAssociation();
            orgAssociation.dataId = outputDataElement.getAttribute("id");
            orgAssociation.columnName = outputDataElement.getAttribute("name");
            orgAssociations.add(orgAssociation);
        }

        //get associations
        NodeList associations = document.getElementsByTagName("association");
        for (int i = 0; i < associations.getLength(); i++){
            Element associationElement = (Element) associations.item(i);
            Element targetRefElement = (Element) associationElement.getElementsByTagName("targetRef").item(0);
            Element sourceRefElement = (Element) associationElement.getElementsByTagName("sourceRef").item(0);
            String targetRef = targetRefElement.getAttribute("href").substring(1);
            for (OrganizationAssociation oa :
                 orgAssociations) {
                if (oa.dataId.equals(targetRef)){
                    oa.textAnnotationId = sourceRefElement.getAttribute("href").substring(1);
                    break;
                }
            }
        }

        //get textAnnotations
        NodeList textAnnotations = document.getElementsByTagName("textAnnotation");
        for (int i = 0; i < textAnnotations.getLength(); i++){
            Element textAnnotationElement = (Element) textAnnotations.item(i);
            for (OrganizationAssociation oa :
                    orgAssociations) {
                if (textAnnotationElement.getAttribute("id").equals(oa.textAnnotationId)){
                    oa.organizationMspid = textAnnotationElement.getElementsByTagName("text").item(0).getTextContent();
                    break;
                }
            }
        }
        for (Input i:
             inputs) {
            for (OrganizationAssociation oa : orgAssociations) {
                if (i.getInputName().equals(oa.columnName)){
                    i.setOrganizationMSP(oa.organizationMspid);
                    break;
                }
            }
        }
        for (Output o:
                outputs) {
            for (OrganizationAssociation oa : orgAssociations) {
                if (o.getOutputName().equals(oa.columnName)){
                    o.setOrganizationsMSPs(Arrays.asList(oa.organizationMspid.split(";")));
                    break;
                }
            }
        }

        DecisionAsset decisionAsset = new DecisionAsset(decisionId, decisionName, hitPolicy, inputs, outputs);

        return decisionAsset;
    }
}