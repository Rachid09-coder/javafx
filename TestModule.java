import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Set;
import java.lang.module.ModuleReference;

public class TestModule {
    public static void main(String[] args) {
        Path path = Path.of(args[0]);
        Set<ModuleReference> refs = ModuleFinder.of(path).findAll();
        for (ModuleReference ref : refs) {
            System.out.println(ref.descriptor().name());
        }
    }
}
