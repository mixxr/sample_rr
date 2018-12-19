/*
 * @author <a href="mailto:serpico@adobe.com">Michelangelo Serpico</a>
 */

package sample_rr.core.resolvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample_rr.core.utils.StringUtils;

/**
 * Test/example ResourceProvider that provides info about Use
 * /carts.tidy.-1.json to GET the whole thing.
 * 
 */

@Component(service = ResourceProvider.class, property = {
        ResourceProvider.PROPERTY_NAME + "=" + CartsResourceProvider.NAME,
        ResourceProvider.PROPERTY_ROOT + "=" + CartsResourceProvider.ROOT,
        ResourceProvider.PROPERTY_MODIFIABLE + "=true" })
public class CartsResourceProvider extends ResourceProvider<Object> {

    private static final Map<String, ValueMap> CARTS = new HashMap<String, ValueMap>();
    private boolean dirtyState = false;
    
    
    /** TODO: This can be configurable */
    public static final String NAME = "Carts";
    public static final String ROOT = "/carts";
    

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static {
        insertDummyCart("Default", 70.10, 2);
        insertDummyCart("Wishlist", 82.00, 3);
        insertDummyCart("XMas 2018", 496.00, 4).put("comment", "we are late!");
        insertDummyCart("My Kids", 79.11, 3).put("comment", "a list for my kids only");
        ;

        // add few items to the default cart
        final String defaultItem1Path = ROOT + "/default/123qwe";
        CARTS.put(defaultItem1Path, new CartResource.CartValueMap("123qwe", 40.10, 1));
        final String defaultItem2Path = ROOT + "/default/iop890";
        CARTS.put(defaultItem2Path, new CartResource.CartValueMap("iop890", 20.00, 2));
    }

    @Override
    public Resource getResource(ResolveContext<Object> ctx, String path, ResourceContext resourceContext,
            Resource parent) {
        
        // Synthetic resource for our root, so that /carts works
        if ((ROOT).equals(path)) {
            return new SyntheticResource(ctx.getResourceResolver(), path, CartResource.RESOURCE_TYPE);
        }

        // Not root, return a Cart if we have one
        final ValueMap data = CARTS.get(path);
        logger.info("--> GET: "+path+", "+((data!=null)?data.size():0));
        return data == null ? null : new CartResource(ctx.getResourceResolver(), path, data);
    }

    @Override
    public Iterator<Resource> listChildren(ResolveContext<Object> ctx, Resource parent) {
        if (parent.getPath().startsWith(ROOT)) {
            // TODO: needs to be optimized
            final List<Resource> kids = new ArrayList<Resource>();
          
            for (Map.Entry<String, ValueMap> e : CARTS.entrySet()) {
                if (parent.getPath().equals(parentPath(e.getKey()))) {
                    kids.add(new CartResource(parent.getResourceResolver(), e.getKey(), e.getValue()));
                }
            }
            return kids.iterator();
        } else {
            return null;
        }
    }

    private static String parentPath(String path) {
        final int lastSlash = path.lastIndexOf("/");
        return lastSlash > 0 ? path.substring(0, lastSlash) : "";
    }

    private static ValueMap insertDummyCart(String name, double value, int itemsCounter) {
    	String newPath = ROOT + "/" + StringUtils.sanitize(name);
    	ValueMap valueMap = new CartResource.CartValueMap(name, value, itemsCounter);

        CARTS.put(newPath, valueMap);
        return valueMap;
    }

    // ResourceProvider.PROPERTY_MODIFIABLE
    public Resource create(ResolveContext<Object> ctx, String path, Map<String, Object> props)
            throws PersistenceException {

        logger.info("------------ CREATE starts:" + path );

        ValueMap valueMap = CARTS.get(path); 
        if (valueMap == null){
        	final String rName = path.split("/")[path.split("/").length-1];
            logger.debug("-- creating a new cart..."+rName);
            valueMap = new CartResource.CartValueMap(rName);
            CARTS.put(path, valueMap);   
            this.dirtyState = true;
        }

        return new CartResource(ctx.getResourceResolver(), path, valueMap);
    }

    @Override
    public void commit(ResolveContext<Object> ctx) throws PersistenceException {
        logger.info("------------ Commit:"+this.dirtyState);
        // just to test the commit/revert mechanism
        this.dirtyState = false;
    }

    @Override
    public void revert(ResolveContext<Object> ctx) {
        logger.info("------------ Revert:"+this.dirtyState);
        this.dirtyState = false;
    }

    public boolean hasChanges(ResolveContext<Object> ctx) {
        logger.info("------------ hasChanges:"+this.dirtyState);
        return this.dirtyState;
    }
}