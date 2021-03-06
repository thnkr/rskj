/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.net;

import org.ethereum.db.ByteArrayWrapper;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * BlockNodeInformation has information about which blocks are known by which blocks,
 * and provides convenient functions to retrieve all the blocks known by a node, and
 * which nodes know a certain block.
 * <p>
 * BlockNodeInformation will only hold a limited amount of blocks and peers. Blocks
 * that aren't accessed frequently will be deleted, as well as peers.
 * Peers will only remember the last maxBlocks blocks that were inserted.
 */
public class BlockNodeInformation {
    private final Map<NodeID, Set<ByteArrayWrapper>> blocksByNode;
    private final LinkedHashMap<ByteArrayWrapper, Set<NodeID>> nodesByBlock;
    private final int MAX_BLOCKS;
    private final int MAX_PEERS;

    public BlockNodeInformation(final int maxBlocks, final int maxPeers) {
        MAX_BLOCKS = maxBlocks;
        MAX_PEERS = maxPeers;

        // Nodes are evicted in Least-recently-accessed order.
        blocksByNode = new LinkedHashMap<NodeID, Set<ByteArrayWrapper>>(MAX_PEERS, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<NodeID, Set<ByteArrayWrapper>> eldest) {
                return size() > MAX_PEERS;
            }
        };
        // Blocks are evicted in Least-recently-accessed order.
        nodesByBlock = new LinkedHashMap<ByteArrayWrapper, Set<NodeID>>(MAX_BLOCKS, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<ByteArrayWrapper, Set<NodeID>> eldest) {
                return size() > MAX_BLOCKS;
            }
        };
    }

    public BlockNodeInformation() {
        this(1000, 50);
    }

    /**
     * addBlockToNode specifies that a given node knows about a given block.
     *
     * @param blockHash the block hash.
     * @param nodeID    the node to add the block to.
     */
    public void addBlockToNode(@Nonnull final ByteArrayWrapper blockHash, @Nonnull final NodeID nodeID) {
        Set<ByteArrayWrapper> nodeBlocks = blocksByNode.get(nodeID);
        if (nodeBlocks == null) {
            // Create a new empty LRUCache for the blocks that a node know.
            // NodeBlocks are evicted in reverse insertion order.
            nodeBlocks = Collections.newSetFromMap(
                    new LinkedHashMap<ByteArrayWrapper, Boolean>() {
                        protected boolean removeEldestEntry(Map.Entry<ByteArrayWrapper, Boolean> eldest) {
                            return size() > MAX_BLOCKS;
                        }
                    }
            );
            blocksByNode.put(nodeID, nodeBlocks);
        }

        Set<NodeID> blockNodes = nodesByBlock.get(blockHash);
        if (blockNodes == null) {
            // Create a new set for the nodes that know about a block.
            // There is no peer eviction, because there are few peers compared to blocks.
            blockNodes = new HashSet<>();
            nodesByBlock.put(blockHash, blockNodes);
        }

        nodeBlocks.add(blockHash);
        blockNodes.add(nodeID);
    }

    /**
     * getBlocksByNode retrieves all the blocks that a given node knows.
     *
     * @param nodeID the node to check.
     * @return all the blocks known by the given nodeID.
     */
    @Nonnull
    public Set<ByteArrayWrapper> getBlocksByNode(@Nonnull final NodeID nodeID) {
        Set<ByteArrayWrapper> result = blocksByNode.get(nodeID);
        if (result == null) {
            result = new HashSet<>();
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * getNodesByBlock retrieves all the nodes that contain a given block.
     *
     * @param blockHash the block's hash.
     * @return A set containing all the nodes that have that block.
     */
    @Nonnull
    public Set<NodeID> getNodesByBlock(@Nonnull final ByteArrayWrapper blockHash) {
        Set<NodeID> result = nodesByBlock.get(blockHash);
        if (result == null) {
            result = new HashSet<>();
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * getNodesByBlock is a convenient function to avoid creating a ByteArrayWrapper.
     *
     * @param blockHash the block hash.
     * @return all the nodeIDs that contain the given block.
     */
    @Nonnull
    public Set<NodeID> getNodesByBlock(@Nonnull final byte[] blockHash) {
        return getNodesByBlock(new ByteArrayWrapper(blockHash));
    }

    /**
     * getBlocksByNode is a convenient function to avoid creating a NodeID.
     *
     * @param nodeID the node id.
     * @return all the hashes of the blocks that the given node knows.
     */
    @Nonnull
    public Set<ByteArrayWrapper> getBlocksByNode(@Nonnull final byte[] nodeID) {
        return getBlocksByNode(new NodeID(nodeID));
    }
}
