package net.village_taverns.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.village_taverns.TavernsMod;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrewTapBlock extends Block {
    public static final String NAME = "barrel";
    public static final Identifier ID = new Identifier(TavernsMod.ID, NAME);

    public BrewTapBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        tooltip.add(Text.translatable("block." + ID.getNamespace() + "." + ID.getPath() +".hint").formatted(Formatting.GRAY, Formatting.ITALIC));
    }

    // MARK: Facing

    private static DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        FACING = Properties.HORIZONTAL_FACING;
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
    }

    // MARK: Partial transparency

    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    // MARK: Shape

    public static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 2, 16, 14, 14);
    public static final VoxelShape SHAPE_R = Block.createCuboidShape(2, 0, 0, 14, 14, 16);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH, SOUTH -> SHAPE_R;
            default -> SHAPE;
        };
    }
}
