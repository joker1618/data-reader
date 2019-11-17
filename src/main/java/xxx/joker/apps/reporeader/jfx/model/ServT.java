package xxx.joker.apps.reporeader.jfx.model;

import org.springframework.stereotype.Service;

import static xxx.joker.libs.core.util.JkConsole.display;

@Service
public class ServT {

    public void doSome() {
        display("doSome {}", getClass().getSimpleName());
    }
}
