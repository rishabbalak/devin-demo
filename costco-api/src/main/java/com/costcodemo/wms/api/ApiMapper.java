package com.costcodemo.wms.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.api.dto.InventoryResponse;
import com.costcodemo.wms.api.dto.ItemResponse;
import com.costcodemo.wms.api.dto.MemberResponse;
import com.costcodemo.wms.api.dto.OrderLineResponse;
import com.costcodemo.wms.api.dto.OrderResponse;
import com.costcodemo.wms.core.domain.InventoryBalance;
import com.costcodemo.wms.core.domain.Item;
import com.costcodemo.wms.core.domain.Member;
import com.costcodemo.wms.core.domain.Order;
import com.costcodemo.wms.core.domain.OrderLine;

/**
 * Maps domain models to the wire shapes.
 *
 * <p>Deliberately separate from the translation the core does over legacy records: that one
 * exists to hide DB2/400 shapes, this one exists to decide what the API is willing to
 * promise. Collapsing them would let a change to the file layout leak into the public
 * contract.
 */
@Component
public class ApiMapper {

    public MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getMemberNumber(),
                member.getName(),
                member.getTier().getLegacyCode(),
                member.getTier().getDisplayName(),
                member.getTier().getAnnualFeeUsd(),
                member.getTier().earnsAnnualReward(),
                member.isActive(),
                member.getJoinDate(),
                member.getRenewalDate(),
                member.getHomeWarehouse());
    }

    public List<MemberResponse> toMemberResponses(List<Member> members) {
        List<MemberResponse> responses = new ArrayList<>();
        for (Member member : members) {
            responses.add(toResponse(member));
        }
        return responses;
    }

    public ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getItemNumber(),
                item.getDescription(),
                item.getDepartment(),
                item.getUnitOfMeasure(),
                item.getPrice(),
                item.isActive());
    }

    public List<ItemResponse> toItemResponses(List<Item> items) {
        List<ItemResponse> responses = new ArrayList<>();
        for (Item item : items) {
            responses.add(toResponse(item));
        }
        return responses;
    }

    public InventoryResponse toResponse(InventoryBalance balance) {
        boolean nettable = balance.isNettable();
        return new InventoryResponse(
                balance.getWarehouseCode(),
                balance.getItemNumber(),
                balance.getLocation(),
                balance.getQuantityOnHand(),
                balance.getQuantityAllocated(),
                nettable ? balance.getQuantityAvailable() : 0,
                balance.getStatusCode(),
                nettable);
    }

    public List<InventoryResponse> toInventoryResponses(List<InventoryBalance> balances) {
        List<InventoryResponse> responses = new ArrayList<>();
        for (InventoryBalance balance : balances) {
            responses.add(toResponse(balance));
        }
        return responses;
    }

    public OrderLineResponse toResponse(OrderLine line) {
        return new OrderLineResponse(
                line.getLineNumber(),
                line.getItemNumber(),
                line.getDescription(),
                line.getQuantityOrdered(),
                line.getQuantityAllocated(),
                line.getQuantityBackordered(),
                line.getUnitOfMeasure(),
                line.getUnitPrice(),
                line.getExtendedAmount());
    }

    public OrderResponse toResponse(Order order) {
        List<OrderLineResponse> lines = new ArrayList<>();
        for (OrderLine line : order.getLines()) {
            lines.add(toResponse(line));
        }
        return new OrderResponse(
                order.getOrderNumber(),
                order.getMemberNumber(),
                order.getWarehouseCode(),
                order.getStatus().getLegacyCode(),
                order.getStatus().getDisplayName(),
                order.getOrderDate(),
                order.getSourceCode(),
                order.getLineCount(),
                order.getOrderTotal(),
                order.hasBackorderedLines(),
                lines);
    }

    public List<OrderResponse> toOrderResponses(List<Order> orders) {
        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orders) {
            responses.add(toResponse(order));
        }
        return responses;
    }
}
