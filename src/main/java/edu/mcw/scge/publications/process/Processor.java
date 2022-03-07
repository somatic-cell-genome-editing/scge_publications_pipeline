package edu.mcw.scge.publications.process;

import edu.mcw.scge.dao.implementation.PublicationDAO;
import edu.mcw.scge.datamodel.Author;
import edu.mcw.scge.datamodel.Reference;

import java.util.List;
import java.util.Map;

public class Processor {

    PublicationDAO publicationDAO=new PublicationDAO();

    public int insertReference( Reference reference) throws Exception {
        int refKey=publicationDAO.getNextKeyFromSequence("PUBLICATION_SEQ");
        reference.setKey(refKey);
        publicationDAO.insertPublication(reference);
        return refKey;
    }
    public void insertAuthor(List<Author> authors, int  refKey) throws Exception {
        for(Author author:authors) {
            int authorKey=publicationDAO.getNextKey("pub_authors_seq");
            author.setKey(authorKey);
            publicationDAO.insertAuthor(author);
            publicationDAO.insertPubAuthorAssociation(refKey,authorKey, 0);
        }

    }

    public void updateArticleIds(Map<String, String> articleIdMap, int refKey,long scgeId) throws Exception {
        for(Map.Entry entry:articleIdMap.entrySet()){
            String idType= (String) entry.getKey();
            String id= (String) entry.getValue();
            publicationDAO.insertPubAssociations(refKey,scgeId,id, idType);
        }
    }
}
