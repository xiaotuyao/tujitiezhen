package org.rabbitemo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod("tujitiezhen")
public class tujitiezhen {
    public static final String MODID = "tujitiezhen"; // 模组ID
    private static int maxAnvilCost = 40; // 最大花费等级限制

    public tujitiezhen() {
        MinecraftForge.EVENT_BUS.register(this); // 注册事件监听
    }

    // 注册指令
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tujitiezhen")
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            int value = IntegerArgumentType.getInteger(context, "value");
                                            setMaxAnvilCost(value);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("[兔子铁砧] 最大花费已设置为: " + value),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
        );
    }

    // 监听并修改铁砧界面
    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof AnvilScreen anvilScreen) {
            try {
                // 通过反射访问 `cost` 字段
                Field costField = AnvilMenu.class.getDeclaredField("cost");
                costField.setAccessible(true);

                // 获取 DataSlot 对象
                DataSlot costSlot = (DataSlot) costField.get(anvilScreen.getMenu());

                // 获取当前花费值
                int currentCost = costSlot.get();

                // 如果当前花费超过限制，强制修改
                if (currentCost > maxAnvilCost) {
                    costSlot.set(maxAnvilCost); // 设置新的花费限制

                    // 向玩家发送聊天提示
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("铁砧花费已被限制为: " + maxAnvilCost),
                            false
                    );
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // 设置花费限制的方法
    public static void setMaxAnvilCost(int value) {
        maxAnvilCost = value;
        System.out.println("[DEBUG] Max Anvil Cost set to: " + value); // 调试日志：最大花费限制
    }
}
