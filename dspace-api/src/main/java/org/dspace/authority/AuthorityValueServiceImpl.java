/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.content.authority.SolrAuthority;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This service contains all methods for using authority values
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class AuthorityValueServiceImpl implements AuthorityValueService {

    private final Logger log = org.apache.logging.log4j.LogManager.getLogger(AuthorityValueServiceImpl.class);

    @Autowired
    protected AuthorityTypes authorityTypes;

    protected AuthorityValueServiceImpl() {

    }

    @Override
    public AuthorityValue generate(String authorityKey, String content, String field) {
        AuthorityValue nextValue = null;

        nextValue = generateRaw(authorityKey, content, field);


        if (nextValue != null) {
            //Only generate a new UUID if there isn't one offered OR if the identifier needs to be generated
            if (StringUtils.isBlank(authorityKey)) {
                // An existing metadata without authority is being indexed
                // If there is an exact match in the index, reuse it before adding a new one.
                List<AuthorityValue> byValue = findByExactValue(field, content);
                if (byValue != null && !byValue.isEmpty()) {
                    authorityKey = byValue.get(0).getId();
                } else {
                    authorityKey = UUID.randomUUID().toString();
                }
            } else if (StringUtils.startsWith(authorityKey, GENERATE)) {
                authorityKey = UUID.randomUUID().toString();
            }

            nextValue.setId(authorityKey);
            nextValue.updateLastModifiedDate();
            nextValue.setCreationDate(Instant.now());
            nextValue.setField(field);
        }

        return nextValue;
    }

    protected AuthorityValue generateRaw(String authorityKey, String content, String field) {
        AuthorityValue nextValue;
        if (authorityKey != null && authorityKey.startsWith(GENERATE)) {
            String[] split = StringUtils.split(authorityKey, SPLIT);
            String type = null;
            String info = null;
            if (split.length > 0) {
                type = split[1];
                if (split.length > 1) {
                    info = split[2];
                }
            }
            AuthorityValue authorityType = authorityTypes.getEmptyAuthorityValue(type);
            nextValue = authorityType.newInstance(info);
        } else {
            Map<String, AuthorityValue> fieldDefaults = authorityTypes.getFieldDefaults();
            nextValue = fieldDefaults.get(field).newInstance(null);
            if (nextValue == null) {
                nextValue = new AuthorityValue();
            }
            nextValue.setValue(content);
        }
        return nextValue;
    }

    @Override
    public AuthorityValue update(AuthorityValue value) {
        AuthorityValue updated = generateRaw(value.generateString(), value.getValue(), value.getField());
        if (updated != null) {
            updated.setId(value.getId());
            updated.setCreationDate(value.getCreationDate());
            updated.setField(value.getField());
            if (updated.hasTheSameInformationAs(value)) {
                updated.setLastModified(value.getLastModified());
            } else {
                updated.updateLastModifiedDate();
            }
        }
        return updated;
    }

    /**
     * Item.ANY does not work here.
     *
     * @param authorityID authority id
     * @return AuthorityValue
     */
    @Override
    public AuthorityValue findByUID(String authorityID) {
        //Ensure that if we use the full identifier to match on
        String queryString = "id:\"" + authorityID + "\"";
        List<AuthorityValue> findings = find(queryString);
        return findings.size() > 0 ? findings.get(0) : null;
    }

    @Override
    public List<AuthorityValue> findByValue(String field, String value) {
        String queryString = "value:" + value + " AND field:" + field;
        return find(queryString);
    }

    @Override
    public AuthorityValue findByOrcidID(String orcid_id) {
        String queryString = "orcid_id:" + orcid_id;
        List<AuthorityValue> findings = find(queryString);
        return findings.size() > 0 ? findings.get(0) : null;
    }

    @Override
    public List<AuthorityValue> findByExactValue(String field, String value) {
        String queryString = "value:\"" + value + "\" AND field:" + field;
        return find(queryString);
    }

    @Override
    public List<AuthorityValue> findByValue(String schema, String element, String qualifier,
                                            String value) {
        String field = fieldParameter(schema, element, qualifier);
        return findByValue(field, value);
    }

    @Override
    public List<AuthorityValue> findByName(String schema, String element, String qualifier,
                                           String name) {
        String field = fieldParameter(schema, element, qualifier);
        String queryString = "first_name:" + name + " OR last_name:" + name + " OR name_variant:" + name + " AND " +
            "field:" + field;
        return find(queryString);
    }

    @Override
    public List<AuthorityValue> findByAuthorityMetadata(String schema, String element,
                                                        String qualifier, String value) {
        String field = fieldParameter(schema, element, qualifier);
        String queryString = "all_Labels:" + value + " AND field:" + field;
        return find(queryString);
    }

    @Override
    public List<AuthorityValue> findOrcidHolders() {
        String queryString = "orcid_id:*";
        return find(queryString);
    }

    @Override
    public List<AuthorityValue> findAll() {
        String queryString = "*:*";
        return find(queryString);
    }

    @Override
    public AuthorityValue fromSolr(SolrDocument solrDocument) {
        String type = (String) solrDocument.getFieldValue("authority_type");
        AuthorityValue value = authorityTypes.getEmptyAuthorityValue(type);
        value.setValues(solrDocument);
        return value;
    }

    @Override
    public AuthorityValue getAuthorityValueType(String metadataString) {
        AuthorityValue fromAuthority = null;
        for (AuthorityValue type : authorityTypes.getTypes()) {
            if (StringUtils.startsWithIgnoreCase(metadataString, type.getAuthorityType())) {
                fromAuthority = type;
            }
        }
        return fromAuthority;
    }

    protected List<AuthorityValue> find(String queryString) {
        List<AuthorityValue> findings = new ArrayList<AuthorityValue>();
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(filtered(queryString));
            log.debug("AuthorityValueFinder makes the query: " + queryString);
            QueryResponse queryResponse = SolrAuthority.getSearchService().search(solrQuery);
            if (queryResponse != null && queryResponse.getResults() != null && 0 < queryResponse.getResults()
                                                                                                .getNumFound()) {
                for (SolrDocument document : queryResponse.getResults()) {
                    AuthorityValue authorityValue = fromSolr(document);
                    findings.add(authorityValue);
                    log.debug("AuthorityValueFinder found: " + authorityValue.getValue());
                }
            }
        } catch (Exception e) {
            log.error("Error while retrieving AuthorityValue from solr. query: " + queryString, e);
        }

        return findings;
    }

    protected String filtered(String queryString) throws InstantiationException, IllegalAccessException {
        String instanceFilter = "-deleted:true";
        if (StringUtils.isNotBlank(instanceFilter)) {
            queryString += " AND " + instanceFilter;
        }
        return queryString;
    }

    protected String fieldParameter(String schema, String element, String qualifier) {
        return schema + "_" + element + ((qualifier != null) ? "_" + qualifier : "");
    }
}
