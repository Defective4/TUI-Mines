package io.github.defective4.javajam.tuisweeper.discord;

import com.sun.jna.Structure;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Defective
 */
public class OrderedStructure extends Structure {
    @Override
    protected List<String> getFieldOrder() {
        return Arrays.stream(getClass().getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
    }
}
