package space.impact.api.kotlinx.consumer;

import space.impact.api.hint.HintColor;
import space.impact.api.structure.IStructureDefinition;
import space.impact.api.structure.StructureDefinition;
import space.impact.api.structure.StructureUtility;

public final class JavaConsumer {
    public static IStructureDefinition<Object> definition() {
        return StructureDefinition.<Object>builder()
                .addShape("main", new String[][] {{"~H"}})
                .addElement('H', StructureUtility.ofHint(HintColor.WHITE))
                .build();
    }
}
