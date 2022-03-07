package edu.mcw.scge.publications;





import edu.mcw.scge.dao.implementation.PublicationDAO;
import edu.mcw.scge.publications.process.Extractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static edu.mcw.scge.publications.process.Extractor.readJsonFromUrl;

public class Manager {

    public static void main(String args[]) throws Exception {
        Extractor extractor=new Extractor();
        PublicationDAO publicationDAO=new PublicationDAO();
        //Loading an existing document
    /*  File file = new File("C:/Users/jthota/Downloads/CRISPR-CAS fie from huge phages is a hypercompact genome editor.pdf");
        PDDocument document = PDDocument.load(file);
        //Instantiate PDFTextStripper class
        PDFTextStripper pdfStripper = new PDFTextStripper();
        //Retrieving text from PDF document
        String text = pdfStripper.getText(document);
        System.out.println(text);
        //Closing the document
        document.close();
*/
  //   String url1 = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=32675376&tool=my_tool&email=my_email@example.com&retmode=xml&rettype=abstract";
 //    JSONObject json = readJsonFromUrl(url1);
//        System.out.println(json.toString());

  //      System.out.println(json.get("pmid"));
      List<Long> pmids=publicationDAO.getAllPMIDs();
     for(long pmid:pmids){
         //  long pmid=32675376;
            //https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=22368089&retmode=xml&rettype=abstract
            String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?"+
                    "db=pubmed" +
                    "&retmode=xml&rettype=abstract"+
                    "&id="+pmid;

            extractor.fetchArticle(pmid, url);
       }


    }

}
