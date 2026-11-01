package space.impact.structures.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.Field;
import java.util.Collections;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;

import org.junit.jupiter.api.Test;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.util.DimensionalCoord;

class WirelessAccessTest {
    @Test
    void rangeDimensionActiveAccessPointAndSecurityAreRequired() throws Exception {
        EntityPlayerMP player = mock(EntityPlayerMP.class);
        World world = mock(World.class);
        Field provider = World.class.getField("provider");
        provider.setAccessible(true);
        provider.set(world, new WorldProviderSurface());
        world.provider.dimensionId = 0;
        player.worldObj = world;
        player.posX = 4;
        IGrid grid = mock(IGrid.class);
        ISecurityGrid security = mock(ISecurityGrid.class);
        when(grid.getCache(ISecurityGrid.class)).thenReturn(security);
        IMachineSet machines = mock(IMachineSet.class);
        when(grid.getMachines(any())).thenReturn(machines);
        IGridNode node = mock(IGridNode.class);
        IGridHost host = mock(IGridHost.class, withSettings().extraInterfaces(IWirelessAccessPoint.class));
        IWirelessAccessPoint access = (IWirelessAccessPoint) host;
        when(node.getMachine()).thenReturn(host);
        when(machines.iterator()).thenAnswer(call -> Collections.singletonList(node).iterator());
        when(access.getLocation()).thenReturn(new DimensionalCoord(world, 0, 0, 0));
        when(access.getRange()).thenReturn(8.0);
        when(access.isActive()).thenReturn(true);
        assertFalse(WirelessExtractor.canAccess(grid, player, true), "Booster must not bypass security");
        when(security.hasPermission(player, SecurityPermissions.EXTRACT)).thenReturn(true);
        assertTrue(WirelessExtractor.canAccess(grid, player, false));
        player.posX = 9;
        assertFalse(WirelessExtractor.canAccess(grid, player, false));
        assertTrue(WirelessExtractor.canAccess(grid, player, true));
        player.posX = 4;
        player.worldObj = mock(World.class);
        assertFalse(WirelessExtractor.canAccess(grid, player, false));
        player.worldObj = world;
        when(access.isActive()).thenReturn(false);
        assertFalse(WirelessExtractor.canAccess(grid, player, false));
    }
}
