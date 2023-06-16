package committee.nova.neutron.command.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import committee.nova.neutron.Neutron;
import committee.nova.neutron.api.player.INeutronPlayer;
import committee.nova.neutron.api.storage.INamedPos;
import committee.nova.neutron.api.storage.IPos;
import committee.nova.neutron.command.perm.PermNode;
import committee.nova.neutron.server.storage.NeutronPersistentState;
import committee.nova.neutron.storage.NamedPos;
import committee.nova.neutron.util.Utilities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandInit {
    private static final Map<String, LiteralCommandNode<CommandSourceStack>> cmds = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        cmds.put("ntnsethome", dispatcher.register(Commands.literal("ntnsethome")
                .then(Commands.argument("home", StringArgumentType.string()).executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    final ServerPlayer player = src.getPlayerOrException();
                    final String name = StringArgumentType.getString(ctx, "home");
                    final boolean added = ((INeutronPlayer) player).addHome(NamedPos.createNamedPos(name, player.level.dimension(), player.position()));
                    if (added)
                        src.sendSuccess(new TranslatableComponent("msg.neutron.home.set.success", name).withStyle(ChatFormatting.GREEN), false);
                    else src.sendFailure(new TranslatableComponent("msg.neutron.home.set.failure"));
                    return added ? 1 : 0;
                }).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HOME_SET, 0)))
                .executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    final ServerPlayer player = src.getPlayerOrException();
                    final INeutronPlayer ntn = (INeutronPlayer) player;
                    final String name = Utilities.getDefault("Home", ntn.getHomes().stream().map(INamedPos::getName).toList());
                    final boolean added = ntn.addHome(NamedPos.createNamedPos(name, player.level.dimension(), player.position()));
                    if (added)
                        src.sendSuccess(new TranslatableComponent("msg.neutron.home.set.success", name).withStyle(ChatFormatting.GREEN), false);
                    else src.sendFailure(new TranslatableComponent("msg.neutron.home.set.failure"));
                    return added ? 1 : 0;
                }).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HOME_SET, 0))));
        cmds.put("ntnhome", dispatcher.register(Commands.literal("ntnhome")
                .then(Commands.argument("home", StringArgumentType.string())
                        .executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final ServerPlayer player = src.getPlayerOrException();
                            final INeutronPlayer ntn = (INeutronPlayer) player;
                            final int cd = ntn.getHomeCd();
                            if (cd > 0) {
                                src.sendFailure(new TranslatableComponent("msg.neutron.tp.cd", Utilities.getActualSecondStr(cd)));
                                return 0;
                            }
                            final String name = StringArgumentType.getString(ctx, "home");
                            for (final var home : ntn.getHomes()) {
                                if (!home.getName().equals(name)) continue;
                                final boolean success = home.teleportPlayer(player, IPos.TeleportationType.HOME);
                                if (success)
                                    src.sendSuccess(new TranslatableComponent("msg.neutron.tp.success.specific", home.getName()).withStyle(ChatFormatting.GREEN), false);
                                else
                                    src.sendFailure(new TranslatableComponent("msg.neutron.tp.failure.specific", home.getName()));
                                return 1;
                            }
                            src.sendFailure(new TranslatableComponent("msg.neutron.loc.not_found", name));
                            return 0;
                        })
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HOME_TP, 0)))
                .executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    final ServerPlayer player = src.getPlayerOrException();
                    final INeutronPlayer ntn = (INeutronPlayer) player;
                    final int cd = ntn.getHomeCd();
                    if (cd > 0) {
                        src.sendFailure(new TranslatableComponent("msg.neutron.tp.cd", Utilities.getActualSecondStr(cd)));
                        return 0;
                    }
                    final List<INamedPos> homes = ntn.getHomes();
                    switch (homes.size()) {
                        case 0 -> {
                            src.sendFailure(new TranslatableComponent("msg.neutron.home.not_set"));
                            return 0;
                        }
                        case 1 -> {
                            final INamedPos home = homes.get(0);
                            final boolean success = home.teleportPlayer(player, IPos.TeleportationType.HOME);
                            if (success)
                                src.sendSuccess(new TranslatableComponent("msg.neutron.tp.success.specific", home.getName()).withStyle(ChatFormatting.GREEN), false);
                            else
                                src.sendFailure(new TranslatableComponent("msg.neutron.tp.failure.specific", home.getName()));
                            return 1;
                        }
                        default -> {
                            src.sendSuccess(new TranslatableComponent("msg.neutron.home.following"), false);
                            homes.forEach(h -> src.sendSuccess(new TextComponent(h.getDesc()).setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.YELLOW)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ntnhome " + h.getName()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.neutron.teleport_to", h.getName())))
                            ), false));
                            return 1;
                        }
                    }
                })
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HOME_TP, 0))));
        cmds.put("ntndelhome", dispatcher.register(Commands.literal("ntndelhome")
                .then(Commands.argument("home", StringArgumentType.string())
                        .executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final ServerPlayer player = src.getPlayerOrException();
                            final INeutronPlayer ntn = (INeutronPlayer) player;
                            final String name = StringArgumentType.getString(ctx, "home");
                            for (final var home : ntn.getHomes()) {
                                if (!home.getName().equals(name)) continue;
                                final boolean success = ntn.removeHome(name);
                                if (success)
                                    src.sendSuccess(new TranslatableComponent("msg.neutron.del.success", name).withStyle(ChatFormatting.GREEN), false);
                                else src.sendFailure(new TranslatableComponent("msg.neutron.del.failure", name));
                                return success ? 1 : 0;
                            }
                            src.sendFailure(new TranslatableComponent("msg.neutron.loc.not_found", name));
                            return 0;
                        })
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HOME_DEL, 0)))
                .executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    final ServerPlayer player = src.getPlayerOrException();
                    final INeutronPlayer ntn = (INeutronPlayer) player;
                    final List<INamedPos> homes = ntn.getHomes();
                    switch (homes.size()) {
                        case 0 -> {
                            src.sendFailure(new TranslatableComponent("msg.neutron.home.not_set"));
                            return 0;
                        }
                        case 1 -> {
                            final INamedPos home = homes.get(0);
                            final boolean success = ntn.removeHome(home.getName());
                            if (success)
                                src.sendSuccess(new TranslatableComponent("msg.neutron.del.success", home.getName()).withStyle(ChatFormatting.GREEN), false);
                            else src.sendFailure(new TranslatableComponent("msg.neutron.del.failure", home.getName()));
                            return 1;
                        }
                        default -> {
                            src.sendSuccess(new TranslatableComponent("msg.neutron.home.following"), false);
                            homes.forEach(h -> src.sendSuccess(new TextComponent(h.getDesc()).setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.DARK_RED)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ntndelhome " + h.getName()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.neutron.del", h.getName())))
                            ), false));
                            return 1;
                        }
                    }
                })
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HOME_DEL, 0))));
        cmds.put("ntnback", dispatcher.register(Commands.literal("ntnback")
                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final ServerPlayer player = src.getPlayerOrException();
                            final INeutronPlayer ntn = (INeutronPlayer) player;
                            final int cd = ntn.getBackCd();
                            if (cd > 0) {
                                src.sendFailure(new TranslatableComponent("msg.neutron.tp.cd", Utilities.getActualSecondStr(cd)));
                                return 0;
                            }
                            final int index = IntegerArgumentType.getInteger(ctx, "index");
                            if (index + 1 >= ntn.getFootprints().size()) {
                                src.sendFailure(new TranslatableComponent("msg.neutron.footprint.not_found"));
                                return 0;
                            }
                            final IPos back = ntn.getFootprints().get(index);
                            final boolean success = back.teleportPlayer(player, IPos.TeleportationType.BACK);
                            if (success)
                                src.sendSuccess(new TranslatableComponent("msg.neutron.tp.success").withStyle(ChatFormatting.GREEN), false);
                            else src.sendFailure(new TranslatableComponent("msg.neutron.tp.failure"));
                            return success ? 1 : 0;
                        })
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_BACK, 0)))
                .executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    final ServerPlayer player = src.getPlayerOrException();
                    final INeutronPlayer ntn = (INeutronPlayer) player;
                    final int cd = ntn.getBackCd();
                    if (cd > 0) {
                        src.sendFailure(new TranslatableComponent("msg.neutron.tp.cd", Utilities.getActualSecondStr(cd)));
                        return 0;
                    }
                    final List<IPos> footprints = ntn.getFootprints();
                    switch (footprints.size()) {
                        case 0 -> {
                            src.sendFailure(new TranslatableComponent("msg.neutron.footprint.none"));
                            return 0;
                        }
                        case 1 -> {
                            final IPos footprint = footprints.get(0);
                            final boolean success = footprint.teleportPlayer(player, IPos.TeleportationType.BACK);
                            if (success)
                                src.sendSuccess(new TranslatableComponent("msg.neutron.tp.success").withStyle(ChatFormatting.GREEN), false);
                            else src.sendFailure(new TranslatableComponent("msg.neutron.tp.failure"));
                            return 1;
                        }
                        default -> {
                            src.sendSuccess(new TranslatableComponent("msg.neutron.footprint.following"), false);
                            final int size = footprints.size();
                            for (int i = 0; i < size; i++) {
                                final IPos footprint = footprints.get(i);
                                src.sendSuccess(new TextComponent((i + 1) + ". " + footprint.getDesc()).setStyle(Style.EMPTY
                                        .withColor(ChatFormatting.YELLOW)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ntnback " + i))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.neutron.teleport_to", footprint.getDesc())))
                                ), false);
                            }
                            return 1;
                        }
                    }
                })
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_BACK, 0))));
        cmds.put("ntnwarp", dispatcher.register(Commands.literal("ntnwarp")
                .then(Commands.argument("warp", StringArgumentType.string())
                        .executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final ServerPlayer player = src.getPlayerOrException();
                            final INeutronPlayer ntn = (INeutronPlayer) player;
                            final int cd = ntn.getHomeCd();
                            if (cd > 0) {
                                src.sendFailure(new TranslatableComponent("msg.neutron.tp.cd", Utilities.getActualSecondStr(cd)));
                                return 0;
                            }
                            final NeutronPersistentState state = NeutronPersistentState.getNeutron(src.getServer());
                            final String name = StringArgumentType.getString(ctx, "warp");
                            for (final var warp : state.getWarps()) {
                                if (!warp.getName().equals(name)) continue;
                                final boolean success = warp.teleportPlayer(player, IPos.TeleportationType.WARP);
                                if (success)
                                    src.sendSuccess(new TranslatableComponent("msg.neutron.tp.success.specific", warp.getName()).withStyle(ChatFormatting.GREEN), false);
                                else
                                    src.sendFailure(new TranslatableComponent("msg.neutron.tp.failure.specific", warp.getName()));
                                return 1;
                            }
                            src.sendFailure(new TranslatableComponent("msg.neutron.loc.not_found", name));
                            return 0;
                        })
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_WARP_TP, 0)))
                .executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    final NeutronPersistentState state = NeutronPersistentState.getNeutron(src.getServer());
                    final List<INamedPos> warps = state.getWarps();
                    if (warps.size() == 0) {
                        src.sendFailure(new TranslatableComponent("msg.neutron.warp.none"));
                        return 0;
                    }
                    src.sendSuccess(new TranslatableComponent("msg.neutron.warp.following"), false);
                    warps.forEach(h -> src.sendSuccess(new TextComponent(h.getDesc()).setStyle(Style.EMPTY
                            .withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ntnwarp " + h.getName()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.neutron.teleport_to", h.getName())))
                    ), false));
                    return 1;
                })
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_WARP_TP, 0))));
        cmds.put("ntnaddwarp", dispatcher.register(Commands.literal("ntnaddwarp")
                .then(Commands.argument("warp", StringArgumentType.string())
                        .executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final ServerPlayer player = src.getPlayerOrException();
                            final String name = StringArgumentType.getString(ctx, "warp");
                            final NeutronPersistentState state = NeutronPersistentState.getNeutron(src.getServer());
                            final boolean added = state.modifyWarps(warps -> warps.add(NamedPos.createNamedPos(name, player.level.dimension(), player.position())));
                            if (added)
                                src.sendSuccess(new TranslatableComponent("msg.neutron.warp.set.success", name).withStyle(ChatFormatting.GREEN), false);
                            else src.sendFailure(new TranslatableComponent("msg.neutron.warp.set.failure"));
                            return added ? 1 : 0;
                        })
                        .requires(p -> Utilities.checkPerm(p, PermNode.ADMIN_WARP_ADD, p.getServer().getOperatorUserPermissionLevel())))
                .executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    final ServerPlayer player = src.getPlayerOrException();
                    final NeutronPersistentState state = NeutronPersistentState.getNeutron(src.getServer());
                    final String name = Utilities.getDefault("Warp", state.getWarps().stream().map(INamedPos::getName).toList());
                    final boolean added = state.modifyWarps(warps -> warps.add(NamedPos.createNamedPos(name, player.level.dimension(), player.position())));
                    if (added)
                        src.sendSuccess(new TranslatableComponent("msg.neutron.warp.set.success", name).withStyle(ChatFormatting.GREEN), false);
                    else src.sendFailure(new TranslatableComponent("msg.neutron.warp.set.failure"));
                    return added ? 1 : 0;
                })
                .requires(p -> Utilities.checkPerm(p, PermNode.ADMIN_WARP_ADD, p.getServer().getOperatorUserPermissionLevel()))));
        cmds.put("ntndelwarp", dispatcher.register(Commands.literal("ntndelwarp")
                .then(Commands.argument("warp", StringArgumentType.string())
                        .executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final NeutronPersistentState state = NeutronPersistentState.getNeutron(src.getServer());
                            final String name = StringArgumentType.getString(ctx, "warp");
                            for (final var warp : state.getWarps()) {
                                if (!warp.getName().equals(name)) continue;
                                final boolean success = state.modifyWarps(warps -> warps.remove(warp));
                                if (success)
                                    src.sendSuccess(new TranslatableComponent("msg.neutron.del.success", name).withStyle(ChatFormatting.GREEN), false);
                                else src.sendFailure(new TranslatableComponent("msg.neutron.del.failure", name));
                                return success ? 1 : 0;
                            }
                            src.sendFailure(new TranslatableComponent("msg.neutron.loc.not_found", name));
                            return 0;
                        })
                        .requires(p -> Utilities.checkPerm(p, PermNode.ADMIN_WARP_DEL, p.getServer().getOperatorUserPermissionLevel())))
                .executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    final NeutronPersistentState state = NeutronPersistentState.getNeutron(src.getServer());
                    final List<INamedPos> warps = state.getWarps();
                    if (warps.size() == 0) {
                        src.sendFailure(new TranslatableComponent("msg.neutron.warp.none"));
                        return 0;
                    }
                    src.sendSuccess(new TranslatableComponent("msg.neutron.warp.following"), false);
                    warps.forEach(h -> src.sendSuccess(new TextComponent(h.getDesc()).setStyle(Style.EMPTY
                            .withColor(ChatFormatting.DARK_RED)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ntndelwarp " + h.getName()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.neutron.del", h.getName())))
                    ), false));
                    return 1;
                })
                .requires(p -> Utilities.checkPerm(p, PermNode.ADMIN_WARP_DEL, p.getServer().getOperatorUserPermissionLevel()))));
        dispatcher.register(Commands.literal("neutron")
                .then(Commands.literal("help").executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    for (final var e : cmds.keySet()) {
                        src.sendSuccess(new TranslatableComponent("desc.neutron." + e).withStyle(ChatFormatting.GREEN), false);
                        final List<String> alias = Neutron.getAlternativesFor(e);
                        if (alias.isEmpty()) continue;
                        src.sendSuccess(new TranslatableComponent("desc.neutron.alias").withStyle(ChatFormatting.YELLOW), false);
                        alias.forEach(a -> src.sendSuccess(new TextComponent("/" + a).withStyle(ChatFormatting.YELLOW), false));
                    }
                    return 1;
                }).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HELP, 0)))
                .then(Commands.literal("reload").executes(ctx -> {
                    final var success = Neutron.reload();
                    ctx.getSource().sendSuccess(new TranslatableComponent("msg.neutron.reload." + (success ? "success" : "failure")
                            .formatted(success ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
                    return success ? 1 : 0;
                }).requires(p -> Utilities.checkPerm(p, PermNode.ADMIN_RELOAD, p.getServer().getOperatorUserPermissionLevel())))
                .requires(p -> true));
        for (final var e : cmds.entrySet()) {
            final var p = e.getValue();
            Neutron.getAlternativesFor(e.getKey()).forEach(a -> dispatcher.register(Commands.literal(a)
                    .redirect(p)
                    .executes(p.getCommand())
                    .requires(p.getRequirement()))
            );
        }
    }
}
