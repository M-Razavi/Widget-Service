package com.miro.sample.board.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.miro.sample.board.WidgetBoardTest;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoFieldShadowingRule;
import com.openpojo.validation.rule.impl.NoNestedClassRule;
import com.openpojo.validation.rule.impl.NoPrimitivesRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsExceptStaticFinalRule;
import com.openpojo.validation.rule.impl.NoStaticExceptFinalRule;
import com.openpojo.validation.rule.impl.SerializableMustHaveSerialVersionUIDRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.jupiter.api.Test;

class WidgetTest implements WidgetBoardTest {

    @Test
    void testClone() {
        Widget widget = newWidget();
        Widget clone = new Widget(widget);

        assertEquals(widget.getId(), clone.getId());
        assertEquals(widget.getX(), clone.getX());
        assertEquals(widget.getY(), clone.getY());
        assertEquals(widget.getZ(), clone.getZ());
        assertEquals(widget.getWidth(), clone.getWidth());
        assertEquals(widget.getHeight(), clone.getHeight());
        assertEquals(widget.getModifiedDate(), clone.getModifiedDate());
    }

    @Test
    void validateWidgetEntity() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(Widget.class);
        Validator validator = ValidatorBuilder
            .create()
            .with(new SetterMustExistRule())
            .with(new GetterMustExistRule())
            .with(new SetterTester())
            .with(new GetterTester())
            .with(new NoPrimitivesRule())
            .with(new NoNestedClassRule())
            .with(new NoStaticExceptFinalRule())
            .with(new SerializableMustHaveSerialVersionUIDRule())
            .with(new NoFieldShadowingRule())
            .with(new NoPublicFieldsExceptStaticFinalRule())
            .build();

        validator.validate(pojoclass);
    }

}