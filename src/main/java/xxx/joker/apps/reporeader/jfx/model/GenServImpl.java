package xxx.joker.apps.reporeader.jfx.model;

import org.springframework.stereotype.Service;

import static xxx.joker.libs.core.util.JkConsole.display;

@Service
class GenServImpl implements GenServ {


    @Override
    public void doSomeWork() {
        display("doSomeWork from {}", getClass().getSimpleName());
    }
}
