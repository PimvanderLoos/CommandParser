package nl.pim16aap2.cap.command;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.util.LocalizedMap;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * The {@link LocalizedMap} for {@link Command}s.
 *
 * @author Pim
 */
public class CommandMap extends LocalizedMap<Command>
{
    protected final @NonNull CAP cap;

    public CommandMap(final @NonNull CAP cap, final int initialCapacity)
    {
        super(cap.getLocalizer(), initialCapacity);
        this.cap = cap;
    }

    public CommandMap(final @NonNull CAP cap)
    {
        super(cap.getLocalizer());
        this.cap = cap;
    }

    /**
     * Gets a {@link Command} from its name.
     *
     * @param nameKey The key for the name of the {@link Command}. See {@link Command#getIdentifier()}.
     * @param locale  The {@link Locale} for which to get the {@link Command}.
     * @return The {@link Command)} with the given name, if it is registered here.
     */
    public @NonNull Optional<Command> getCommand(@Nullable String nameKey, @Nullable Locale locale)
    {
        return getEntry(nameKey, locale, cap::getCommandNameCaseCheck);
    }

    /**
     * Adds the provided command for every locale.
     *
     * @param command The {@link Command} to register.
     */
    public void addCommand(final @NonNull Command command)
    {
        addEntry((IGNOREME, locale) -> command.getName(locale), command, cap::getCommandNameCaseCheck);
    }
}
