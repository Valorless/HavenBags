package valorless.havenbags.datamodels;

/**
 * Simple data model that describes an external plugin tag used by AutoPickup.
 * <p>
 * Instances represent how a plugin identifies its items or data, allowing
 * AutoPickup.checkPlugins() to recognize and interoperate with those plugins.
 * Each tag provides a human-friendly name, a namespace, and a specific
 * Persistent Data Container (PDC) key string.
 *
 * @see AutoPickup#checkPlugins()
 */
public class PluginTags {
	
	/** Plugin name (as shown in it's plugin.yml, or the Console). */
	public String name;
	/** Namespace used in filters.yml, such as "<code>nexo:</code>". */
	public String namespace;
	/** PDC key string used to detect or mark items belonging to the plugin. */
	public String pdcKey;
	
	/**
	 * Create a new plugin tag descriptor.
	 * @param name Plugin name (as shown in it's plugin.yml, or the Console)
	 * @param namespace Namespace used in filters.yml, such as "<code>nexo:</code>"
	 * @param pdcKey Persistent Data Container key string for plugin-specific data
	 */
	public PluginTags(String name, String namespace, String pdcKey) {
		this.name = name;
		this.namespace = namespace;
		this.pdcKey = pdcKey;
	}
}