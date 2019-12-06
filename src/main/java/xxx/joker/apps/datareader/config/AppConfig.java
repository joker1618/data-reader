package xxx.joker.apps.datareader.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import xxx.joker.apps.datareader.jfx.model.GuiModel;

@Configuration
//@Profile("")
public class AppConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

//    @Bean
//    public GuiModel guiModel() {
//        LOG.debug("Created bean 'guiModel'");
//        return new GuiModelImpl();
//    }

}
