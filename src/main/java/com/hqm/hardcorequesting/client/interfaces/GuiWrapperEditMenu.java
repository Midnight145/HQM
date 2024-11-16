package com.hqm.hardcorequesting.client.interfaces;

import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiWrapperEditMenu extends GuiBase {

    private GuiEditMenu editMenu;

    @Override
    public void setEditMenu(GuiEditMenu editMenu) {

        if (editMenu != null) {
            this.editMenu = editMenu;
        } else {
            this.mc.displayGuiScreen(null);
        }
    }

    public static final ResourceLocation BG_TEXTURE = ResourceHelper.getResource("wrapper");
    public static final ResourceLocation C_BG_TEXTURE = ResourceHelper.getResource("c_wrapper");

    private static final int TEXTURE_WIDTH = 170;
    private static final int TEXTURE_HEIGHT = 234;

    @Override
    public void drawScreen(int mX0, int mY0, float f) {
        final boolean doublePage = this.editMenu.doesRequiredDoublePage();

        this.left = (this.width - (doublePage ? TEXTURE_WIDTH * 2 : TEXTURE_WIDTH)) / 2;
        this.top = (this.height - TEXTURE_HEIGHT) / 2;

        this.applyColor(0xFFFFFFFF);

        ResourceHelper.bindResource(BG_TEXTURE);

        this.drawRect(0, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        if (doublePage) {
            this.drawRect(TEXTURE_WIDTH, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, RenderRotation.FLIP_HORIZONTAL);
        }

        this.applyColor(0xFFFFFFFF);

        ResourceHelper.bindResource(MAP_TEXTURE);

        final int mX = mX0 - this.left;
        final int mY = mY0 - this.top;

        if (this.editMenu != null) {
            this.editMenu.draw(this, mX, mY);
            this.editMenu.drawMouseOver(this, mX, mY);
        }
    }

    @Override
    protected void mouseClicked(int mX0, int mY0, int b) {
        final int mX = mX0 - this.left;
        final int mY = mY0 - this.top;

        if (this.editMenu != null) {
            this.editMenu.onClick(this, mX, mY, b);
        }
    }

    @Override
    protected void mouseClickMove(int mX0, int mY0, int b, long ticks) {
        final int mX = mX0 - this.left;
        final int mY = mY0 - this.top;

        if (this.editMenu != null) {
            this.editMenu.onDrag(this, mX, mY);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mX0, int mY0, int b) {
        final int mX = mX0 - this.left;
        final int mY = mY0 - this.top;

        if (this.editMenu != null) {
            this.editMenu.onRelease(this, mX, mY);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        if (this.editMenu != null) {
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth - this.left;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - this.top;

            final int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                this.editMenu.onScroll(this, x, y, scroll);
            }
        }
    }

    @Override
    protected void keyTyped(char c, int k) {
        super.keyTyped(c, k);

        if (this.editMenu != null) {
            this.editMenu.onKeyTyped(this, c, k);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
