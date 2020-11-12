package nl.pim16aap2.cap.argument;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.parser.ArgumentParser;
import nl.pim16aap2.cap.argument.parser.ValuelessParser;
import nl.pim16aap2.cap.argument.validator.IArgumentValidator;
import nl.pim16aap2.cap.argument.validator.number.RangeValidator;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.util.TabCompletionRequest;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents an argument that can be used by a {@link Command}.
 *
 * @param <T> The type of object the argument input is parsed into. This can be an integer, or a string, or anything
 *            else that can be parsed from a String.
 */
public class Argument<T>
{
    /**
     * The short name of this {@link Argument}.
     */
    protected final @NonNull String shortName;

    /**
     * The (optional) long name of this {@link Argument}. For example, a help command usually has the short name '-h'
     * and the long name '--help'.
     */
    protected final @Nullable String longName;

    /**
     * A short summary to describe what this argument does and/or how it is used.
     */
    protected @NonNull String summary;

    /**
     * The {@link ArgumentParser} that is used to parse the argument into the desired type.
     */
    @Getter(AccessLevel.PROTECTED)
    protected @NonNull ArgumentParser<T> parser;

    /**
     * The label of the this {@link Argument}. For example, in the case of '[-p=player]', "player" would be the label.
     * <p>
     * This is also the name by which this Argument's result can be retrieved.
     */
    protected final @NonNull String label;

    /**
     * The default value for this {@link Argument}.
     * <p>
     * If this {@link Argument} is required, this will not be used. If it is optional, this is the value that will be
     * returned in case the argument input did not have a value for this {@link Argument}.
     */
    @Getter
    protected final @Nullable T defaultValue;

    /**
     * Whether or not this {@link Argument} is repeatable.
     *
     * @see RepeatableArgument
     */
    @Getter
    protected final boolean repeatable;

    /**
     * Whether or not this argument is valueless.
     * <p>
     * A valueless argument is one where its presence basically is the value.
     * <p>
     * An example of a valueless argument would be '--staged' in 'git diff --staged'
     */
    @Getter
    protected final boolean valuesLess;

    /**
     * Whether or not this {@link Argument} is positional.
     * <p>
     * The position is determined by the order in which the {@link Argument}s are added to the {@link Command}.
     */
    @Getter
    protected final boolean positional;

    /**
     * Whether or not this {@link Argument} is required.
     */
    @Getter
    protected final boolean required;

    /**
     * The {@link Supplier} to use to provide completion suggestions.
     * <p>
     * For example, if this {@link Argument} requires a playername, it could provide a list of names of nearby players.
     */
    @Getter
    protected final @Nullable ITabCompleteFunction tabCompleteFunction;

    /**
     * The {@link IArgumentValidator} to use to make sure that the input value meets certain constraints.
     * <p>
     * For example, this can be used to make sure some integer is in the range of [10, 20] (see {@link
     * RangeValidator#integerRangeValidator(int, int)}).
     */
    @Getter
    protected final @Nullable IArgumentValidator<T> argumentValidator;

    /**
     * The identifier to use for retrieving results.
     */
    @Getter
    protected final @NonNull String identifier;

    /**
     * @param shortName           {@link #shortName}.
     * @param longName            {@link #longName}.
     * @param summary             {@link #summary}.
     * @param parser              {@link #parser}.
     * @param defaultValue        {@link #defaultValue}.
     * @param label               {@link #label}.
     * @param valuesLess          {@link #valuesLess}.
     * @param repeatable          {@link #repeatable}.
     * @param positional          {@link #positional}.
     * @param required            {@link #required}.
     * @param tabCompleteFunction {@link #tabCompleteFunction}.
     * @param argumentValidator   {@link #argumentValidator}.
     * @param identifier          {@link #identifier}.
     */
    protected Argument(final @NonNull String shortName, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser, final @Nullable T defaultValue,
                       final @NonNull String label, final boolean valuesLess, final boolean repeatable,
                       final boolean positional, final boolean required,
                       final @Nullable ITabCompleteFunction tabCompleteFunction,
                       final @Nullable IArgumentValidator<T> argumentValidator, final @NonNull String identifier)
    {
        this.shortName = shortName;
        this.longName = longName;
        this.summary = summary;
        this.parser = parser;
        this.defaultValue = defaultValue;
        this.label = label;
        this.valuesLess = valuesLess;
        this.repeatable = repeatable;
        this.positional = positional;
        this.required = required;
        this.tabCompleteFunction = tabCompleteFunction;
        this.argumentValidator = argumentValidator;
        this.identifier = identifier;
    }

    /**
     * Creates a new required and positional {@link Argument}.
     *
     * @param shortName           {@link #shortName}.
     * @param longName            {@link #longName}.
     * @param summary             {@link #summary}.
     * @param parser              {@link #parser}.
     * @param tabCompleteFunction {@link #tabCompleteFunction}.
     * @param argumentValidator   {@link #argumentValidator}.
     * @param identifier          {@link #identifier}.
     */
    @Builder(builderMethodName = "requiredBuilder", builderClassName = "RequiredBuilder")
    protected Argument(final @NonNull String shortName, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser,
                       final @Nullable ITabCompleteFunction tabCompleteFunction,
                       final @Nullable IArgumentValidator<T> argumentValidator, final @NonNull String identifier)
    {
        this(shortName, longName, summary, parser, null, "", false, false, true, true, tabCompleteFunction,
             argumentValidator, identifier);
    }

    /**
     * Creates a new optional and positional {@link Argument}.
     *
     * @param shortName           {@link #shortName}.
     * @param longName            {@link #longName}.
     * @param summary             {@link #summary}.
     * @param parser              {@link #parser}.
     * @param tabCompleteFunction {@link #tabCompleteFunction}.
     * @param argumentValidator   {@link #argumentValidator}.
     * @param identifier          {@link #identifier}.
     */
    @Builder(builderMethodName = "optionalPositionalBuilder", builderClassName = "OptionalPositionalBuilder")
    protected Argument(final @NonNull String shortName, final @Nullable String longName, final @NonNull String summary,
                       final @Nullable ITabCompleteFunction tabCompleteFunction,
                       final @NonNull ArgumentParser<T> parser,
                       final @Nullable IArgumentValidator<T> argumentValidator, final @NonNull String identifier)
    {
        this(shortName, longName, summary, parser, null, "", false, false, true, false, tabCompleteFunction,
             argumentValidator, identifier);
    }

    /**
     * Creates a new optional {@link Argument}.
     *
     * @param shortName           {@link #shortName}.
     * @param longName            {@link #longName}.
     * @param summary             {@link #summary}.
     * @param parser              {@link #parser}.
     * @param defaultValue        {@link #defaultValue}.
     * @param label               {@link #label}.
     * @param tabCompleteFunction {@link #tabCompleteFunction}.
     * @param argumentValidator   {@link #argumentValidator}.
     * @param identifier          {@link #identifier}.
     */
    @Builder(builderMethodName = "optionalBuilder", builderClassName = "OptionalBuilder")
    protected Argument(final @NonNull String shortName, final @Nullable String longName, final @NonNull String summary,
                       final @NonNull ArgumentParser<T> parser, final @Nullable T defaultValue,
                       final @NonNull String label, final @Nullable ITabCompleteFunction tabCompleteFunction,
                       final @Nullable IArgumentValidator<T> argumentValidator, final @NonNull String identifier)
    {
        this(shortName, longName, summary, parser, defaultValue, label, false, false, false, false, tabCompleteFunction,
             argumentValidator, identifier);
    }

    /**
     * Creates a new valueless {@link Argument}. See {@link #valuesLess}.
     *
     * @param shortName  {@link #shortName}.
     * @param longName   {@link #longName}.
     * @param summary    {@link #summary}.
     * @param value      Whether the presence of this flag means 'true' or 'false'. Default: True
     * @param identifier {@link #identifier}.
     */
    @SuppressWarnings("unchecked")
    @Builder(builderMethodName = "privateValuesLessBuilder", builderClassName = "ValuesLessBuilder")
    private Argument(final @NonNull String shortName, final @Nullable String longName, final @NonNull String summary,
                     final @Nullable Boolean value, final @NonNull String identifier)
    {
        this(shortName, longName, summary,
             (ArgumentParser<T>) ValuelessParser.create(Util.valOrDefault(value, Boolean.TRUE)),
             (T) (Boolean) (!Util.valOrDefault(value, Boolean.TRUE)), "", true, false, false, false, null, null,
             identifier);
    }

    /**
     * Valueless {@link Argument}s should always have {@link Boolean} as their type, so the generic builder is private
     * and only the boolean builder can be used. See {@link #valuesLessBuilder()}.
     *
     * @param <T> Always a {@link Boolean}.
     * @return A new builder for a valueless {@link Argument}.
     */
    private static <T> ValuesLessBuilder<T> privateValuesLessBuilder()
    {
        return new ValuesLessBuilder<>();
    }

    /**
     * Creates a new builder for a valueless {@link Argument}. See {@link #valuesLess}.
     *
     * @return A new builder for a valueless {@link Argument}
     */
    public static @NonNull ValuesLessBuilder<Boolean> valuesLessBuilder()
    {
        return Argument.privateValuesLessBuilder();
    }

    /**
     * Gets the short name of this {@link Argument}.
     *
     * @param cap    The {@link CAP} instance to use for localization.
     * @param locale The {@link Locale} to use.
     * @return The localized short name.
     */
    public final @NonNull String getShortName(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        return cap.getMessage(shortName, locale);
    }

    /**
     * Gets the (optional) long name of this {@link Argument}. For example, a help command usually has the short name
     * '-h' and the long name '--help'.
     *
     * @param cap    The {@link CAP} instance to use for localization.
     * @param locale The {@link Locale} to use.
     * @return The localized long name.
     */
    public final @Nullable String getLongName(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        return longName == null ? null : cap.getMessage(longName, locale);
    }

    /**
     * Gets the key for the short name of this {@link Argument}.
     *
     * @return The key for the short name.
     */
    public final @NonNull String getShortNameKey()
    {
        return shortName;
    }

    /**
     * Gets the key for the long name of this {@link Argument}.
     *
     * @return The key for the long name.
     */
    public final @Nullable String getLongNameKey()
    {
        return longName;
    }

    /**
     * Gets the short summary to describe what this argument does and/or how it is used.
     *
     * @param cap    The {@link CAP} instance to use for localization.
     * @param locale The {@link Locale} to use.
     * @return The localized summary.
     */
    public final @NonNull String getSummary(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        return cap.getMessage(summary, locale);
    }

    /**
     * Gets the label of the this {@link Argument}. For example, in the case of '[-p=player]', "player" would be the
     * label.
     * <p>
     * This is also the name by which this Argument's result can be retrieved.
     *
     * @param cap    The {@link CAP} instance to use for localization.
     * @param locale The {@link Locale} to use.
     * @return The localized label.
     */
    public final @NonNull String getLabel(final @NonNull CAP cap, final @Nullable Locale locale)
    {
        return cap.getMessage(label, locale);
    }

    /**
     * Parses the input using {@link #parser} and validates it using the {@link #argumentValidator} if it is provided.
     *
     * @param value         The value to parse and validate.
     * @param cap           The {@link CAP} that requested the argument to be parsed.
     * @param commandSender The {@link ICommandSender} for which to parse validate the argument.
     * @return The parsed value.
     *
     * @throws ValidationFailureException If the value was not valid. See {@link IArgumentValidator#validate(CAP,
     *                                    ICommandSender, Argument, Object)}.
     */
    protected @NonNull T parseArgument(final @NonNull String value, final @NonNull CAP cap,
                                       final @NonNull ICommandSender commandSender)
        throws ValidationFailureException
    {
        final @NonNull T parsed = parser.parseArgument(value);

        if (argumentValidator != null)
            argumentValidator.validate(cap, commandSender, this, parsed);

        return parsed;
    }

    /**
     * Parses the input and stores the result in an {@link IParsedArgument}. See {@link #parseArgument(String, CAP,
     * ICommandSender)}
     *
     * @param value         The value to parse and validate.
     * @param cap           The {@link CAP} that requested the argument to be parsed.
     * @param commandSender The {@link ICommandSender} for which to parse validate the argument.
     * @return The parsed argument.
     */
    public @NonNull IParsedArgument<?> getParsedArgument(final @Nullable String value, final @NonNull CAP cap,
                                                         final @NonNull ICommandSender commandSender)
        throws ValidationFailureException
    {
        if (value == null)
            return new ParsedArgument<>(defaultValue);
        return new ParsedArgument<>(parseArgument(value, cap, commandSender));
    }

    /**
     * Gets a {@link IParsedArgument} using the {@link #defaultValue}.
     *
     * @return A {@link IParsedArgument} using the {@link #defaultValue}.
     */
    public @NonNull IParsedArgument<?> getDefault()
    {
        return new ParsedArgument<>(defaultValue);
    }

    /**
     * Represents the results of parsing an argument.
     *
     * @param <T> The type of the parsed result.
     * @author Pim
     */
    public interface IParsedArgument<T>
    {
        /**
         * Gets the value as it was parsed from the arguments.
         *
         * @return The parsed value.
         */
        @Nullable
        T getValue();

        /**
         * Updates the currently stored value with a new value.
         *
         * @param value The new value to store.
         */
        <U> void updateValue(final @Nullable U value);
    }

    /**
     * The implementation of {@link IParsedArgument} for regular {@link Argument}s.
     *
     * @param <T> The type of the parsed result.
     * @author Pim
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ParsedArgument<T> implements IParsedArgument<T>
    {
        @Getter(onMethod = @__({@Override}))
        protected @Nullable T value;

        @Override
        @SuppressWarnings("unchecked")
        public <U> void updateValue(final @Nullable U newValue)
        {
            value = (T) newValue;
        }
    }

    /**
     * Represents a {@link BiFunction} that receives an {@link ICommandSender} and an {@link Argument}.
     * <p>
     * The function is supposed to return a list of suggestions to use as values for the {@link Argument} for the given
     * {@link ICommandSender}.
     */
    @FunctionalInterface
    public interface ITabCompleteFunction
        extends Function<@NonNull TabCompletionRequest, @NonNull List<@NonNull String>>
    {
    }
}
