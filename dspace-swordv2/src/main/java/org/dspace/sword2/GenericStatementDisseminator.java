/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.sword2;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.swordapp.server.OriginalDeposit;
import org.swordapp.server.ResourcePart;
import org.swordapp.server.Statement;

public abstract class GenericStatementDisseminator
    implements SwordStatementDisseminator {
    protected SwordUrlManager urlManager;

    protected ItemService itemService = ContentServiceFactory.getInstance()
                                                             .getItemService();

    protected void populateStatement(Context context, Item item,
                                     Statement statement)
        throws DSpaceSwordException {
        this.urlManager = new SwordUrlManager(new SwordConfigurationDSpace(),
                                              context);
        List<String> includeBundles = this.getIncludeBundles();
        String originalDepositBundle = this.getOriginalDepositsBundle();

        // we only list the original deposits in full if the sword bundle is in the includeBundles
        if (includeBundles.contains(originalDepositBundle)) {
            List<OriginalDeposit> originalDeposits = this
                .getOriginalDeposits(context, item, originalDepositBundle);
            statement.setOriginalDeposits(originalDeposits);
        }

        Map<String, String> states = this.getStates(context, item);
        statement.setStates(states);

        // remove the original deposit bundle from the include bundles
        includeBundles.remove(originalDepositBundle);
        List<ResourcePart> resources = this
            .getResourceParts(context, item, includeBundles);
        statement.setResources(resources);

        Instant lastModified = this.getLastModified(context, item);
        statement.setLastModified(java.util.Date.from(lastModified));
    }

    protected List<OriginalDeposit> getOriginalDeposits(Context context,
                                                        Item item, String swordBundle)
        throws DSpaceSwordException {
        try {
            // NOTE: DSpace does not store file metadata, so we can't access the information
            // about who deposited what, when, on behalf of whoever.

            List<OriginalDeposit> originalDeposits = new ArrayList<OriginalDeposit>();

            // an original deposit is everything in the SWORD bundle
            List<Bundle> bundles = item.getBundles();
            for (Bundle bundle : bundles) {
                if (swordBundle.equals(bundle.getName())) {
                    List<Bitstream> bitstreams = bundle
                        .getBitstreams();
                    for (Bitstream bitstream : bitstreams) {
                        // note that original deposits don't have actionable urls
                        OriginalDeposit deposit = new OriginalDeposit(
                            this.urlManager.getBitstreamUrl(
                                bitstream));
                        deposit.setMediaType(bitstream
                                                 .getFormat(context).getMIMEType());
                        deposit.setDepositedOn(java.util.Date.from(this.getDateOfDeposit(item)));
                        originalDeposits.add(deposit);
                    }
                }
            }
            return originalDeposits;
        } catch (SQLException e) {
            throw new DSpaceSwordException(e);
        }
    }

    protected Map<String, String> getStates(Context context, Item item)
        throws DSpaceSwordException {
        SwordConfigurationDSpace config = new SwordConfigurationDSpace();
        WorkflowTools wft = new WorkflowTools();
        Map<String, String> states = new HashMap<String, String>();
        if (item.isWithdrawn()) {
            String uri = config.getStateUri("withdrawn");
            String desc = config.getStateDescription("withdrawn");
            states.put(uri, desc);
        } else if (item.isArchived()) {
            String uri = config.getStateUri("archive");
            String desc = config.getStateDescription("archive");
            states.put(uri, desc);
        } else if (wft.isItemInWorkflow(context, item)) {
            String uri = config.getStateUri("workflow");
            String desc = config.getStateDescription("workflow");
            states.put(uri, desc);
        } else if (wft.isItemInWorkspace(context, item)) {
            String uri = config.getStateUri("workspace");
            String desc = config.getStateDescription("workspace");
            states.put(uri, desc);
        }
        return states;
    }

    protected List<ResourcePart> getResourceParts(Context context, Item item,
                                                  List<String> includeBundles)
        throws DSpaceSwordException {
        try {
            // the list of resource parts is everything in the bundles to be included
            List<ResourcePart> resources = new ArrayList<ResourcePart>();

            for (String bundleName : includeBundles) {
                List<Bundle> bundles = item.getBundles();
                for (Bundle bundle : bundles) {
                    if (bundleName.equals(bundle.getName())) {
                        List<Bitstream> bitstreams = bundle
                            .getBitstreams();
                        for (Bitstream bitstream : bitstreams) {
                            // note that individual bitstreams have actionable urls
                            ResourcePart part = new ResourcePart(this.urlManager
                                                                     .getActionableBitstreamUrl(
                                                                         bitstream));
                            part.setMediaType(bitstream
                                                  .getFormat(context).getMIMEType());
                            resources.add(part);
                        }
                    }
                }
            }

            return resources;
        } catch (SQLException e) {
            throw new DSpaceSwordException(e);
        }
    }

    protected Instant getLastModified(Context context, Item item) {
        return item.getLastModified();
    }

    private List<String> getIncludeBundles() {
        String[] bundles = DSpaceServicesFactory.getInstance().getConfigurationService()
                                                .getArrayProperty("swordv2-server.statement.bundles");
        if (ArrayUtils.isEmpty(bundles)) {
            bundles = new String[] {"ORIGINAL", "SWORD"};
        }
        List<String> include = new ArrayList<String>();
        for (String bit : bundles) {
            String bundleName = bit.trim().toUpperCase();
            if (!include.contains(bundleName)) {
                include.add(bundleName);
            }
        }
        return include;
    }

    private String getOriginalDepositsBundle() {
        String swordBundle = DSpaceServicesFactory.getInstance().getConfigurationService()
                                                  .getProperty("swordv2-server.bundle.name");
        if (swordBundle == null) {
            swordBundle = "SWORD";
        }
        return swordBundle;
    }

    private Instant getDateOfDeposit(Item item) {
        List<MetadataValue> values = itemService.getMetadata(item, "dc", "date", "accessioned", Item.ANY);
        Instant date = Instant.now();
        if (values != null && values.size() > 0) {
            String strDate = values.get(0).getValue();
            date = new DCDate(strDate).toDate().toInstant();
        }
        return date;
    }
}
