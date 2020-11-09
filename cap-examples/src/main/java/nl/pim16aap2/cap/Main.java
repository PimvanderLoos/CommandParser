package nl.pim16aap2.cap;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.StringArgument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.exception.ExceptionHandler;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextComponent;
import nl.pim16aap2.cap.text.TextType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Make sure that async permission checking is allowed for Spigot. Currently, when using async tab-completion
//       suggestions generation, the permissions are checked asynchronously. That may or may not be problematic.
// TODO: Let the Spigot module load async-generated tab-completion suggestions into the cache for synchronized usage
//       (as Spigot doesn't have an async tab-complete event).
// TODO: Allow the use of empty lines. For Spigot (and probably other platforms?) '\n' isn't good enough.
//       Instead, Spigot needs a color code on an otherwise empty line to have empty lines.
//       Perhaps this can be done via the color scheme?
// TODO: Support ResourceBundle.
// TODO: Currently, the commands are kinda stored in a tree shape (1 super, n subs). Perhaps store it in an actual tree?
// TODO: For the long help, maybe fall back to the summary if no description is available?
// TODO: Should Optional arguments be wrapped inside Optional as well? Might be nice.
// TODO: Unit tests.
// TODO: ValidationFailureException should get the received value and the instance of the validator.
//       The validator will need a (localizable) toString method (or something) to indicate what would be valid values.
//       For the range validator, a validator of [10 20] should return "[10 20]" so inform the user why their value
//       could not be validated.
// TODO: Make sure that positional arguments fed in the wrong order gets handled gracefully
//       (there are probably going to be some casting issues).
//       Also, be more strict in the positional argument parsing. All positional arguments must come before any free ones.
//       Currently, the positional arguments are counted separately, but this breaks the tab completion.
// TODO: Make sure that autocomplete works if all the current string is empty and all positional arguments
//       have already been filled (just return args list).
// TODO: Maybe store the arguments by their label inside the CommandResult? That would avoid confusion of name vs longName.
// TODO: Be more consistent in naming help menus. There should be a clear distinction between the command-specific long help
//       and the command's list of subcommands. Maybe help text and help menu?
//       Alternatively, don't make a distinction at all. The help text could just be page 0 of the help menu?
// TODO: IllegalValueException is only used for OOB page values, so maybe rename it to something more specific to that?
// TODO: Don't throw and EOFException when preprocessing input arguments for tab complete. It should be allowed to
//       tab-complete values split by spaces as well.
// TODO: Add some safeguards for required optional parameters. If it's '/command [pos0] [pos1] <pos2> [pos3]',
//       you cannot know which arguments were provided from "/command val val". So if 1 optional positional argument is
//       provided, no other positional arguments should be allowed.
//       It is possible to have some way of mixing this stuff, but that would require too many rules and just get
//       confusing and bug-prone very fast.
// TODO: Combining short flags into single argument. E.g. '/command -a -b -c' would be equivalent to '/command -abc'
// TODO: Optional repeating positional?? `/bigdoors opendoor door_0 door_1 ... door_x`?
// TODO: If valueless flags have been provided already, don't suggest them again.
//       E.g. when giving "/bigdoors addowner mydoor --admin -", it shouldn't suggest "-a" or "--admin" again, as using
//       that flag again won't do anything.
// TODO: Maybe keep track of the number of argument prefixes? So the CommandParser knows that it should suggest "--admin"
//       or "-a" for "--a".
// TODO: The CommandParser should be split up into 3 classes (it's too fat atm):
//       1) CommandParser, doing all the parsing and shit.
//       2) An input class to preprocess the input and to keep track of whether or not the input is valid (quotation marks etc)
//       3) A tab-suggestion class. This class extends the CommandParser (or maybe it only needs the input class. Prefer composition here)
// TODO: Most of the tab suggestion code in the CAP class can be moved into the cache class. Just let the cache class
//       take care of getting the results on its own. Maybe rename it to make it clear that it's not a cache.
//       Also, this component will have to become required, but the caching aspect can become optional.

public class Main
{
    private static @NonNull String arrToString(final @NonNull String... args)
    {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        sb.append("=============\n").append("Arguments:");
        for (String arg : args)
        {
            sb.append(" \"").append(arg).append("\"");
            sb2.append(" ").append(arg);
        }
        return sb.append(", total: \"/").toString() + sb2.append("\"").substring(1);
    }

    private static void tabComplete(final @NonNull CAP cap, final @NonNull String command)
    {
        System.out.println(command + ":\n");
        final DefaultCommandSender commandSender = new DefaultCommandSender();

        final @NonNull List<String> tabOptions = cap.getTabCompleteOptions(commandSender, command);
        final StringBuilder sb = new StringBuilder();
        sb.append("Tab complete options:\n");
        tabOptions.forEach(opt -> sb.append(opt).append("\n"));
        System.out.println(sb.toString());
        System.out.println("=============\n");
    }

    private static void tryArgs(final @NonNull CAP cap, final @NonNull String command)
    {
        System.out.println(command + ":\n");
        final DefaultCommandSender commandSender = new DefaultCommandSender();
//        commandSender.setColorScheme(Main.getColorScheme());

        cap.parseInput(commandSender, command).ifPresent(CommandResult::run);
        System.out.println("=============\n");
    }

    private static void testSubStrings()
    {
        ColorScheme colorScheme = ColorScheme.builder().build();
        Text text = new Text(colorScheme);
        text.add("123456789");
        System.out.println(text.subsection(0, 3));
        System.out.println(text.subsection(3, 6));
        System.out.println(text.subsection(6, 9));

    }

    public static void main(final String... args)
    {
//        testTextComponents();
        final CAP cap = initCommandManager();
//        testHelpRenderer(commandManager);
//        testSubStrings();

        Text textA = new Text(getColorScheme()).add("D E F", TextType.COMMAND).add(" ");
        Text textB = new Text(getColorScheme()).add("A B C", TextType.REGULAR_TEXT).add(" ");

        Text textC = new Text(getColorScheme()).add("D E F", TextType.COMMAND).add(" ");
        Text textD = new Text(getColorScheme()).add("A B C", TextType.REGULAR_TEXT).add(" ");

        Text textE = new Text(getColorScheme()).add("A B C", TextType.REGULAR_TEXT).add(" ");

        System.out.println(textA.prepend(textB));
        System.out.println(textD.add(textC));
        System.out.println(textE.add(textE));

        tabComplete(cap, "big");
        tabComplete(cap, "add");
        tabComplete(cap, "bigdoors a");
        tabComplete(cap, "bigdoors \"a");
        tabComplete(cap, "bigdoors h");
        tabComplete(cap, "bigdoors subcomma");
        tabComplete(cap, "bigdoors addowner myDoor -p=pim16aap2");
        tabComplete(cap, "bigdoors addowner myDoor --play");
        tabComplete(cap, "bigdoors addowner mydoor --admin ");
        tabComplete(cap, "bigdoors addowner \"tes");

        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2");
        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2 -p=pim16aap3 -p=pim16aap4");
        tryArgs(cap, "bigdoors addowner myDoor --player=pim16aap2");
        tryArgs(cap, "bigdoors addowner myDoor --player=pim16aap2 --admin");
        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner myD\\\"oor -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner \"myD\\\"oor\" -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner \"myD\\\" oor\" -p=\"pim16\"aap2 -a");
        tryArgs(cap, "bigdoors addowner 'myDoor' -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner 'myDoor' -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner -h");
        tryArgs(cap, "bigdoors addowner myDoor -p=\"pim16 \"aap2 -a");

        tryArgs(cap, "bigdoors help addowner");
        tryArgs(cap, "bigdoors help");
        tryArgs(cap, "bigdoors help 1");
        tryArgs(cap, "bigdoors help 2");
        tryArgs(cap, "bigdoors help 6");
        tryArgs(cap, "bigdoors help");
        tryArgs(cap, "bigdoors addowner");
        tryArgs(cap, "bigdoors");
        tryArgs(cap, "bigdoors 1");
        tryArgs(cap, "bigdoors 2");
    }

    private static CAP initCommandManager()
    {
        final CAP cap = CAP
            .builder()
            .separator('=')
            .debug(true)
            .exceptionHandler(ExceptionHandler.getDefault().toBuilder()
                                              .handler(nl.pim16aap2.cap.exception.NonExistingArgumentException.class,
                                                       (sender, ex) -> ex.printStackTrace())
                                              .handler(nl.pim16aap2.cap.exception.IllegalValueException.class,
                                                       (sender, ex) -> ex.printStackTrace())
                                              .handler(nl.pim16aap2.cap.exception.CommandNotFoundException.class,
                                                       (sender, ex) -> ex.printStackTrace())
                                              .handler(nl.pim16aap2.cap.exception.MissingArgumentException.class,
                                                       (sender, ex) -> ex.printStackTrace())
                                              .handler(nl.pim16aap2.cap.exception.NoPermissionException.class,
                                                       (sender, ex) -> ex.printStackTrace())
                                              .handler(nl.pim16aap2.cap.exception.ValidationFailureException.class,
                                                       (sender, ex) -> ex.printStackTrace())
                                              .build())
            .helpCommandRenderer(DefaultHelpCommandRenderer
                                     .builder()
                                     .firstPageSize(1)
                                     .build())
            .build();

        final int subsubCommandCount = 5;
        final List<Command> subsubcommands = new ArrayList<>(subsubCommandCount);
        for (int idx = 0; idx < subsubCommandCount; ++idx)
        {
            final String command = "subsubcommand_" + idx;
            final Command generic = Command
                .commandBuilder().name(command)
                .addDefaultHelpArgument(true)
                .cap(cap)
                .summary("This is the summary for subsubcommand_" + idx)
                .argument(new StringArgument().getRequired().name("value").summary("random value").build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subsubcommands.add(generic);
        }

        final Command addOwner = Command
            .commandBuilder()
            .cap(cap)
            .name("addowner")
            .addDefaultHelpArgument(true)
            .description("Add 1 or more players or groups of players as owners of a door.")
            .summary("Add another owner to a door.")
            .subCommands(subsubcommands)
            .permission(((commandSender, command) -> true))
            .argument(new StringArgument()
                          .getRequired()
                          .name("doorID")
                          .summary("The name or UID of the door")
                          .tabcompleteFunction(request -> Arrays.asList("test a", "test_b"))
                          .build())
            .argument(Argument.valuesLessBuilder()
                              .value(true)
                              .name("a")
                              .longName("admin")
                              .summary("Makes all the supplied users admins for the given door. " +
                                           "Only applies to players.")
                              .build())
            .argument(new StringArgument()
                          .getRepeatable()
                          .name("p")
                          .longName("player")
                          .label("player")
                          .summary("The name of the player(s) to add as owner")
                          .build())
            .argument(new StringArgument()
                          .getRepeatable()
                          .name("g")
                          .longName("group")
                          .label("group")
                          .summary("The name of the group(s) to add as owner")
                          .build())
            .commandExecutor(
                commandResult ->
                    new AddOwner(commandResult.getParsedArgument("doorID"),
                                 commandResult.getParsedArgument("p"),
                                 commandResult.<List<String>>getParsedArgument("g"),
                                 commandResult.getParsedArgument("a")).runCommand())
            .build();

        final int subCommandCount = 20;
        final List<Command> subcommands = new ArrayList<>(subCommandCount);
        for (int idx = 0; idx < subCommandCount; ++idx)
        {
            final String command = "subcommand_" + idx;
            final Command generic = Command
                .commandBuilder().name(command)
                .addDefaultHelpArgument(true)
                .cap(cap)
                .argument(new StringArgument().getRequired().name("value").summary("random value").build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subcommands.add(generic);
        }

        final Command bigdoors = Command
            .commandBuilder()
            .cap(cap)
            .addDefaultHelpSubCommand(true)
            .name("bigdoors")
            .headerSupplier(commandSender -> new Text(commandSender.getColorScheme())
                .add("Parameters in angled brackets are required: ", TextType.HEADER)
                .add("<", TextType.REQUIRED_PARAMETER)
                .add("parameter", TextType.REQUIRED_PARAMETER_LABEL)
                .add(">", TextType.REQUIRED_PARAMETER).add("\n")

                .add("Parameters in square brackets are optional: ", TextType.HEADER)
                .add("[", TextType.OPTIONAL_PARAMETER)
                .add("parameter", TextType.OPTIONAL_PARAMETER_LABEL)
                .add("]", TextType.OPTIONAL_PARAMETER).add("\n")

                .add("If an argument is followed by a \"+\" symbol, it can be\n" +
                         "repeated as many times as you want. For example, for a\n" +
                         "hypothetical command \"", TextType.HEADER)
                .add("/command ", TextType.COMMAND)
                .add("[", TextType.OPTIONAL_PARAMETER)
                .add("p", TextType.OPTIONAL_PARAMETER_FLAG)
                .add("=", TextType.OPTIONAL_PARAMETER_SEPARATOR)
                .add("player", TextType.OPTIONAL_PARAMETER_LABEL)
                .add("]+", TextType.OPTIONAL_PARAMETER)

                .add("\", you can do: \n\"", TextType.HEADER)
                .add("/command -p=playerA -p=playerB", TextType.REGULAR_TEXT)
                .add("\".\n", TextType.HEADER)
                .add(" ", TextType.REGULAR_TEXT)
                .toString())

            .subCommand(addOwner)
            .subCommands(subcommands)
            .virtual(true)
            .build();

        subcommands.forEach(cap::addCommand);
        subsubcommands.forEach(cap::addCommand);
        cap.addCommand(addOwner).addCommand(bigdoors);

        return cap;
    }

    static ColorScheme getClearColorScheme()
    {
        return ColorScheme.builder().build();
    }

    static ColorScheme getColorScheme()
    {
        return ColorScheme
            .builder()
            .setDefaultDisable(MinecraftStyle.RESET.getStringValue())
            .commandStyle(new TextComponent(MinecraftStyle.GOLD.getStringValue()))
            .optionalParameterStyle(new TextComponent(MinecraftStyle.BLUE.getStringValue() +
                                                          MinecraftStyle.BOLD.getStringValue()))
            .optionalParameterFlagStyle(new TextComponent(MinecraftStyle.LIGHT_PURPLE.getStringValue()))
            .optionalParameterSeparatorStyle(new TextComponent(MinecraftStyle.DARK_RED.getStringValue()))
            .optionalParameterLabelStyle(new TextComponent(MinecraftStyle.DARK_AQUA.getStringValue()))
            .requiredParameterStyle(new TextComponent(MinecraftStyle.RED.getStringValue() +
                                                          MinecraftStyle.BOLD.getStringValue()))
            .requiredParameterFlagStyle(new TextComponent(MinecraftStyle.DARK_BLUE.getStringValue()))
            .requiredParameterSeparatorStyle(new TextComponent(MinecraftStyle.BLACK.getStringValue()))
            .requiredParameterLabelStyle(new TextComponent(MinecraftStyle.WHITE.getStringValue()))
            .summaryStyle(new TextComponent(MinecraftStyle.AQUA.getStringValue()))
            .regularTextStyle(new TextComponent(MinecraftStyle.DARK_PURPLE.getStringValue()))
            .headerStyle(new TextComponent(MinecraftStyle.GREEN.getStringValue()))
            .footerStyle(new TextComponent(MinecraftStyle.DARK_RED.getStringValue()))
            .build();
    }

    static void testTextComponents()
    {
        final ColorScheme colorScheme = getColorScheme();

        {
            final Text text = new Text(colorScheme);
            text.add("Unstyled text!");
            System.out.println(text);
            System.out.println(text.add(text));
        }

        final Text text = new Text(colorScheme);
        text.add("This is a command", TextType.COMMAND).add("\n")
            .add("This is an optional parameter", TextType.OPTIONAL_PARAMETER).add("\n")
            .add("This is something else? I can't remember the types :(", TextType.HEADER).add("\n");


        final Text text2 = new Text(colorScheme);
        text2.add("This is the second Text!", TextType.REQUIRED_PARAMETER).add("\n");
        text2.add("This is some unstyled text!").add("\n");

        System.out.println(text.add(text2).toString());
    }
}
