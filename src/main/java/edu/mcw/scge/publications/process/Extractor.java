package edu.mcw.scge.publications.process;

import com.google.gson.Gson;


import edu.mcw.scge.datamodel.Author;
import edu.mcw.scge.datamodel.Reference;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.*;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Extractor {

    Processor processor=new Processor();
    public void extractPDF(String filePath) throws IOException {
    /*    File file = new File(filePath);
        PDDocument document = PDDocument.load(file);
        //Instantiate PDFTextStripper class
        PDFTextStripper pdfStripper = new PDFTextStripper();
        //Retrieving text from PDF document
        String text = pdfStripper.getText(document);
        System.out.println(text);
        //Closing the document
        document.close();*/
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
    public String getInputFile(long pmid, String url) throws IOException {

        String fileName = "data/" + pmid + ".txt";
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception
        }
        return fileName;
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            Gson gson   =new Gson();

           System.out.println("JOSNTEXT:"+ gson.toJson(jsonText));


            return null;
        } finally {
            is.close();
        }
    }
    public String fetchArticle(long pmid, String url) throws IOException {

       //    InputStream is = new URL(url).openStream();
       //    BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
       //  System.out.println(readAll(rd));

        try {
                File inputFile = new File(getInputFile(pmid,url));
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputFile);
                doc.getDocumentElement().normalize();
                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                NodeList nList = doc.getElementsByTagName("Article");
                System.out.println("----------------------------");
                Reference reference=new Reference();
              for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    System.out.println("\nCurrent Element :" + nNode.getNodeName());

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String title=eElement
                                .getElementsByTagName("ArticleTitle")
                                .item(0)
                                .getTextContent();
                       System.out.println("ArticleTitle : "
                                +title);
                       reference.setTitle(title);
                       String abstractText=eElement
                               .getElementsByTagName("AbstractText")
                               .item(0)
                               .getTextContent();
                       reference.setRefAbstract(abstractText);
                        System.out.println("Abstract : "
                                + abstractText);
                        reference.setPubDate(getPubDate(eElement));
                        int refKey= processor.insertReference(reference);
                        processor.insertAuthor(parseAuthorList(eElement), refKey);
                        Map<String, String> articleIdMap=parseArticleIdList(doc);
                        if(articleIdMap.get("doi")!=null)
                        reference.setDoi(articleIdMap.get("doi"));
                        processor.updateArticleIds(parseArticleIdList(doc), refKey);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



       return null;
    }
    public Date getPubDate(Element eElement){

        String year=eElement
                .getElementsByTagName("Year")
                .item(0)
                .getTextContent();
        System.out.println("PubDate Year : "
                + year);
        String month=eElement
                .getElementsByTagName("Month")
                .item(0)
                .getTextContent();

        System.out.println("PubDate Month: "
                + month);
        String day=eElement
                .getElementsByTagName("Day")
                .item(0)
                .getTextContent();
        return makeDateFromString(year,month,day);
    }
    private Date makeDateFromString(String year, String month, String day) {

        if( year==null || year.isEmpty() )
            return null;
        if( month==null || month.isEmpty() )
            month = "Jan";
        if( day==null || day.isEmpty() )
            day = "01";

        Date dt;
        try {
            dt = _dateFormat.parse(year+month+day);
        }
        catch(ParseException e) {
            dt = null;
        }
        return dt;
    }
    static DateFormat _dateFormat = new SimpleDateFormat("yyyyMMMdd");

    List<Author> parseAuthorList(Element eElement) {
        List<Author> authorsList = new ArrayList<Author>();
        NodeList authorList= eElement.getElementsByTagName("Author");
        for (int temp = 0; temp < authorList.getLength(); temp++) {
            Node nNode = authorList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Author author=new Author();
                Element authorElement = (Element) nNode;
                String lastName=authorElement
                        .getElementsByTagName("LastName")
                        .item(0)
                        .getTextContent();
                System.out.println("LastName : "
                        +lastName);
                author.setLastName(lastName);
                String firstName=authorElement
                        .getElementsByTagName("ForeName")
                        .item(0)
                        .getTextContent();

                System.out.println("First Name : "
                        + firstName);
                author.setFirstName(firstName);
                String initials=authorElement
                        .getElementsByTagName("Initials")
                        .item(0)
                        .getTextContent();

                System.out.println("Initials : "
                        + initials);
                author.setInitials(initials);

                authorsList.add(author);
            }

        }

        return authorsList;
    }
    Map<String, String> parseArticleIdList(Document doc){
        Map<String, String> articleIdMap=new HashMap<>();
        NodeList pubmedData= doc.getElementsByTagName("PubmedData");
        for (int temp = 0; temp < pubmedData.getLength(); temp++) {
            Node nNode = pubmedData.item(temp);
            NodeList childNodes=nNode.getChildNodes();
            for(int i=0;i<childNodes.getLength();i++){
                if(childNodes.item(i).getNodeName().equals("ArticleIdList")){
                    Node nodeList=childNodes.item(i);
                    for(int j=0;j<nodeList.getChildNodes().getLength();j++) {
                      Node  articleIdNode=nodeList.getChildNodes().item(j);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element articleIdElement = (Element) articleIdNode;
                            String idType = articleIdElement.getAttribute("IdType");
                            String id = articleIdElement.getTextContent();
                            System.out.println("IDTYPE:"+idType+"\tID:"+id);
                            articleIdMap.put(idType, id);
                        }
                    }
                }
            }
        }

       return articleIdMap;
    }
}
