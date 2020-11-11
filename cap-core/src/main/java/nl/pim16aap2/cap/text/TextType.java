package nl.pim16aap2.cap.text;

import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;

/**
 * Represents the different types of {@link Text} used by CAP. Every type of text can have its own {@link
 * TextComponent}.
 *
 * @author Pim
 */
public enum TextType
{
    /**
     * The name(s) of a command.
     */
    COMMAND,

    /**
     * The name of the value of an optional parameter. E.g. For [-p=player] this would be the '[]' part.
     */
    OPTIONAL_PARAMETER,

    /**
     * The name of the value of an optional parameter. E.g. For [-p=player] this would be the 'player' part.
     */
    OPTIONAL_PARAMETER_LABEL,

    /**
     * The name/flag of an optional parameter. E.g. For [-p=player] this would be the '-p' part.
     */
    OPTIONAL_PARAMETER_FLAG,

    /**
     * The separator of an optional parameter. E.g. For [-p=player] this would be the '=' part.
     */
    OPTIONAL_PARAMETER_SEPARATOR,

    /**
     * A required parameter.
     */
    REQUIRED_PARAMETER,

    /**
     * The name of the value of a required parameter. E.g. For <-p=player> this would be the 'player' part.
     */
    REQUIRED_PARAMETER_LABEL,

    /**
     * The name/flag of a required parameter. E.g. For <-p=player> this would be the '-p' part.
     */
    REQUIRED_PARAMETER_FLAG,

    /**
     * The separator of a required parameter. E.g. For <-p=player> this would be the '=' part.
     */
    REQUIRED_PARAMETER_SEPARATOR,

    /**
     * The {@link Command#getSummary(ICommandSender)} of a {@link Command}.
     */
    SUMMARY,

    /**
     * The {@link Command#getDescription(ICommandSender)} of a {@link Command}.
     */
    DESCRIPTION,

    /**
     * Regular text used for miscellaneous text.
     */
    REGULAR_TEXT,

    /**
     * The {@link Command#getHeader(ICommandSender)} of a {@link Command}.
     */
    HEADER,

    /**
     * The section header. Used for command-specific section headers.
     */
    SECTION,

    /**
     * UNUSED for now.
     */
    FOOTER,

    /**
     * Used for errors and stuff.
     */
    ERROR,
    ;
}
