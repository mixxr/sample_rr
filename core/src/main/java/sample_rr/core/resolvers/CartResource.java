/*
 * @author <a href="mailto:serpico@adobe.com">Michelangelo Serpico</a>
 */

package sample_rr.core.resolvers; 

import java.util.HashMap;

import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ModifiableValueMapDecorator;
import org.apache.sling.adapter.annotations.Adaptable;
import org.apache.sling.adapter.annotations.Adapter;

/** A Sling Resource that represents a cart */
@Adaptable(adaptableClass=Resource.class, adapters={
    @Adapter({ModifiableValueMap.class})
})
public class CartResource extends AbstractResource implements Resource {

    private final String path;
    private final ResourceMetadata metadata;
    private final ValueMap valueMap;
    private final ResourceResolver resolver;
    
    public static final String RESOURCE_TYPE = "sling/test-services/cart";
    
    static class CartValueMap extends ModifiableValueMapDecorator {
        CartValueMap(String name, double totValue, int nItems) {
            super(new HashMap<String, Object>());
            //put("name", name);
            put("totValue", totValue);
            put("nItems", nItems);
            put("jcr:primaryType", "nt:unstructured");
        }
        CartValueMap(String name) {
            super(new HashMap<String, Object>());
            //put("name", name);
            put("jcr:primaryType", "nt:unstructured");
        }
    }
   
    
    CartResource(ResourceResolver resolver, String path, ValueMap valueMap) {
        this.path = path;
                
        this.valueMap = valueMap;
        this.resolver = resolver;
        
        metadata = new ResourceMetadata();
        metadata.setResolutionPath(path);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + path;
    }
    
    public String getPath() {
        return path;
    }

    public ResourceMetadata getResourceMetadata() {
        return metadata;
    }

    public ResourceResolver getResourceResolver() {
        return resolver;
    }

    public String getResourceSuperType() {
        return null;
    }

    public String getResourceType() {
        return RESOURCE_TYPE;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if(type == ModifiableValueMap.class || type == ValueMap.class) {
            return (AdapterType)valueMap;
        }
        return super.adaptTo(type);
    }
}