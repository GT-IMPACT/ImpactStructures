package space.impact.structures.kotlinx.consumer;

import space.impact.structures.hint.HintColor;
import space.impact.structures.structure.IStructureDefinition;
import space.impact.structures.structure.StructureDefinition;
import space.impact.structures.structure.StructureUtility;

public final class JavaConsumer {
    public static IStructureDefinition<Object> definition() {
        return StructureDefinition.<Object>builder()
                .addShape("main", new String[][] {{"~H"}})
                .addElement('H', StructureUtility.withHint(StructureUtility.ofHint(HintColor.WHITE), HintColor.BLUE))
                .build();
    }
}
