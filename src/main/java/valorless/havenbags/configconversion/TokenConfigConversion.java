package valorless.havenbags.configconversion;

import valorless.havenbags.Main;
import valorless.havenbags.annotations.DoNotCall;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.config.Config;

public class TokenConfigConversion {
	
	@DoNotCall("Internal Use Only")
	public static void check(Config config) {
		if(config.GetInt("config-version") < 5) {
    		Log.Warning(Main.plugin, "Old configuration found, updating configs!");
    		config.Set("config-version", 5);
    		config.SaveConfig();
    		
    		if(config.HasKey("skin-token.material")) {
				config.Set("token.skin.material", config.Get("skin-token.material"));
				config.Set("token.skin.custommodeldata", config.Get("skin-token.custommodeldata"));
				config.Set("token.skin.displayname", config.Get("skin-token.display-name"));
				config.Set("token.skin.lore", config.Get("skin-token.lore"));
				
				config.Set("skin-token.display-name", null);
				config.Set("skin-token.material", null);
				config.Set("skin-token.custommodeldata", null);
				config.Set("skin-token.lore", null);
				config.Set("skin-token", null);
				
				config.SaveConfig();
			}
    	}
	}
}
