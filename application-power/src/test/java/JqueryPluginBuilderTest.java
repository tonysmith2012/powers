import com.power.builder.JqueryPluginBuilder;
import com.power.utils.FileUtils;

public class JqueryPluginBuilderTest {

    public static void main(String[] args) {

        JqueryPluginBuilder builder = new JqueryPluginBuilder();
        String str = builder.writeBuilder("FormBuilder");
       // System.out.println(str);
        FileUtils.writeFileNotAppend(str,"d:\\jquery-formbuilder.js");
    }
}
