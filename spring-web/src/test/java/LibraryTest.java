/*
 * This Java source file was generated by the Gradle 'init' task.
 */

import com.google.common.base.Optional;
import org.junit.Test;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.parsertokens.Token;
import org.mockito.internal.matchers.Not;

import javax.validation.*;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class LibraryTest {
    @Test public void testSomeLibraryMethod() throws NoSuchMethodException {
        Expression eh = new Expression("(5^2 * 7^3) * 11^1 * 67^1 * 49201^1");
        List<Token> copyOfInitialTokens = eh.getCopyOfInitialTokens();
        Constant constant = eh.getConstant(0);

        test(null);

    }
   // @Valid
    private void test(@NotNull Integer a) throws NoSuchMethodException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Method[] declaredMethods = this.getClass().getDeclaredMethods();
        for(Method i:declaredMethods)
        {
            i.setAccessible(true);
        }
      //  Method setName = this.getClass()
       //         .getMethod("test", Integer.class);
        Method test1 = this.getClass().getDeclaredMethod("test", Integer.class);
        Set<ConstraintViolation<LibraryTest>> test = validator.forExecutables().validateParameters(this,test1 , new Object[]{a});


    }

}
