package com.miro.sample.board.dto;

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

class WidgetDtoTest implements WidgetBoardTest {

    @Test
    void validateWidgetDto() {
        PojoClass pojoclass = PojoClassFactory.getPojoClass(WidgetDto.class);
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