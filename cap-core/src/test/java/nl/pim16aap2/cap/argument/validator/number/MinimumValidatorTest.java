package nl.pim16aap2.cap.argument.validator.number;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MinimumValidatorTest
{
    public static final @NonNull ICommandSender commandSender = new DefaultCommandSender();
    public static final @NonNull Argument<?> argument = Argument.valuesLessBuilder().shortName("a").summary("")
                                                                .identifier("a").build();
    public static final @NonNull CAP cap = CAP.getDefault();

    @Test
    void validateRangeInteger()
    {
        final @NonNull MinimumValidator<Integer> minimumValidator = MinimumValidator.integerMinimumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> minimumValidator.validate(cap, commandSender, argument, 10));

        Assertions.assertDoesNotThrow(() -> minimumValidator.validate(cap, commandSender, argument, 11));
    }

    @Test
    void validateRangeDouble()
    {
        final @NonNull MinimumValidator<Double> minimumValidator = MinimumValidator.doubleMinimumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> minimumValidator.validate(cap, commandSender, argument, 10.0));

        Assertions.assertDoesNotThrow(() -> minimumValidator.validate(cap, commandSender, argument, 11.0));
    }

    /**
     * A {@link RangeValidator.ValueRequest} that returns a specific value.
     *
     * @param ret The value to return.
     * @return The specified value.
     */
    private @NonNull Integer minimumSupplier(final @NonNull Integer ret, final @NonNull CAP cap,
                                             final @NonNull ICommandSender commandSender, @NonNull Argument<?> argument)
    {
        return ret;
    }

    @Test
    void validateDynamic()
    {
        final @NonNull Integer minimum = 10;
        final @NonNull MinimumValidator<Integer> minimumSupplier = MinimumValidator.integerMinimumValidator(
            (cap1, commandSender1, argument1) -> minimumSupplier(minimum, cap1, commandSender1, argument1));

        Assertions.assertThrows(ValidationFailureException.class,
                                () -> minimumSupplier.validate(cap, commandSender, argument, minimum));

        Assertions.assertDoesNotThrow(() -> minimumSupplier.validate(cap, commandSender, argument, minimum + 1));
    }
}
