package com.olsonsolution.common.spring.domain.test;

import com.olsonsolution.common.spring.application.datasource.item.entity.CategoryData;
import com.olsonsolution.common.spring.application.datasource.item.entity.ItemCategoryData;
import com.olsonsolution.common.spring.application.datasource.item.entity.ItemData;
import com.olsonsolution.common.spring.application.datasource.item.entity.support.ItemCategory;
import com.olsonsolution.common.spring.application.datasource.item.entity.support.ItemType;
import com.olsonsolution.common.spring.application.datasource.item.repository.CategoryJpaRepository;
import com.olsonsolution.common.spring.application.datasource.item.repository.ItemCategoryJpaRepository;
import com.olsonsolution.common.spring.application.datasource.item.repository.ItemJpaRepository;
import com.olsonsolution.common.spring.application.datasource.order.entity.OrderData;
import com.olsonsolution.common.spring.application.datasource.order.repository.OrderJpaRepository;
import com.olsonsolution.common.spring.application.hibernate.EmbeddedTimestamp;
import com.olsonsolution.common.spring.application.test.config.SpringApplicationJpaTestBase;
import com.olsonsolution.common.spring.domain.model.context.SystemContextType;
import com.olsonsolution.common.spring.domain.model.context.TenantThreadLocalContext;
import com.olsonsolution.common.spring.domain.model.context.ThreadLocalContextMetadata;
import com.olsonsolution.common.spring.domain.model.tenant.DomainTenant;
import com.olsonsolution.common.spring.domain.port.repository.context.LocalContextManager;
import com.olsonsolution.common.spring.domain.port.sterotype.context.TenantContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.Duration;
import org.joda.time.MutableDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class TenantJpaSupportTest extends SpringApplicationJpaTestBase {

    @Autowired
    private LocalContextManager localContextManager;

    @Autowired
    private ItemJpaRepository itemJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private ItemCategoryJpaRepository itemCategoryJpaRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @AfterEach
    void clearDataSourceSpec() {
        localContextManager.clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SQLSERVER_TENANT", "POSTGRES_TENANT", "MARIADB_TENANT"})
    void shouldSaveTestData(String tenantId) {
        setTenantThread(tenantId);
        CategoryData category = createCategory();
        categoryJpaRepository.saveAndFlush(category);
        category = categoryJpaRepository.findById(category.getId()).orElseThrow();
        ItemData packageItem = createPackage();
        itemJpaRepository.saveAndFlush(packageItem);
        packageItem = itemJpaRepository.findById(packageItem.getId()).orElseThrow();
        List<ItemData> products = itemJpaRepository.saveAllAndFlush(createProducts(10, packageItem.getId()));
        List<ItemCategoryData> itemCategories = new ArrayList<>(products.size());
        for (ItemData product : products) {
            ItemCategoryData productCategory =
                    new ItemCategoryData(new ItemCategory(category.getId(), product.getId()));
            itemCategories.add(productCategory);
        }
        itemCategories = itemCategoryJpaRepository.saveAllAndFlush(itemCategories);
        MutableDateTime now = MutableDateTime.now();
        MutableDateTime completeUntil = now.copy();
        completeUntil.add(Duration.standardDays(10));
        MutableDateTime deliverUntil = now.copy();
        deliverUntil.add(Duration.standardDays(30));
        List<OrderData> order = products.stream()
                .map(ItemData::getId)
                .map(id -> OrderData.newOrder()
                        .itemId(id)
                        .id("ORDER-" + UUID.randomUUID())
                        .quantity(10L)
                        .fromClient("ABC")
                        .deliverUntil(new EmbeddedTimestamp(deliverUntil))
                        .completeUntil(new EmbeddedTimestamp(completeUntil))
                        .build())
                .toList();
        orderJpaRepository.saveAllAndFlush(order);
        assertThat(itemCategories).isNotEmpty();
    }

    private void setTenantThread(String tenantId) {
        TenantContext tenantContext = TenantThreadLocalContext.tenantContextBuilder()
                .tenant(new DomainTenant(tenantId))
                .id("test")
                .type(SystemContextType.TEST)
                .metadata(ThreadLocalContextMetadata.builder()
                        .startTimestamp(MutableDateTime.now())
                        .build())
                .build();
        localContextManager.setThreadLocal(tenantContext);
    }

    private List<ItemData> createProducts(int qty, String packageId) {
        return IntStream.range(0, qty)
                .mapToObj(i -> createProduct(i, packageId))
                .toList();
    }

    private CategoryData createCategory() {
        return CategoryData.newCategory()
                .code(100)
                .fullName("Test Category")
                .shortName(RandomStringUtils.secure().next(120, true, true))
                .description("Test Category")
                .build();
    }

    private ItemData createProduct(int number, String packageId) {
        return ItemData.newItem()
                .id("ITEM" + number)
                .fullName("Test Item ITEM" + number)
                .shortName("I" + number)
                .description("Test Item")
                .type(ItemType.PRODUCT)
                .packagingId(packageId)
                .xDimension(38)
                .yDimension(15)
                .zDimension(8)
                .netWeight(150.10)
                .grossWeight(150.25)
                .build();
    }

    private ItemData createPackage() {
        return ItemData.newItem()
                .id("BOX40X20X10")
                .fullName("Box 40x20x10")
                .shortName("B40X20X10")
                .description("Standard Box")
                .type(ItemType.PACKAGE)
                .xDimension(40)
                .yDimension(20)
                .zDimension(10)
                .netWeight(0.15)
                .grossWeight(0.15)
                .build();
    }

}
