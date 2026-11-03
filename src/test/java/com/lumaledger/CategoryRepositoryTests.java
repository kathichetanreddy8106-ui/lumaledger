
package com.lumaledger;

import com.lumaledger.model.Category;
import com.lumaledger.model.TransactionType;
import com.lumaledger.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryRepositoryTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void createAndFindCategory() {
        String uniqueName = "Test Category " + System.nanoTime();

        Category category = new Category();
        category.setName(uniqueName);
        category.setType(TransactionType.EXPENSE);

        Category saved = categoryRepository.create(category);

        assertNotNull(saved.getId());
        assertEquals(uniqueName, saved.getName());
        assertEquals(TransactionType.EXPENSE, saved.getType());

        Optional<Category> found =
                categoryRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(uniqueName, found.get().getName());

        categoryRepository.deleteById(saved.getId());
    }

    @Test
    void findByTypeReturnsMatchingCategory() {
        String uniqueName = "Test Income " + System.nanoTime();

        Category category = new Category();
        category.setName(uniqueName);
        category.setType(TransactionType.INCOME);

        Category saved = categoryRepository.create(category);

        try {
            assertTrue(
                    categoryRepository.findByType(TransactionType.INCOME)
                            .stream()
                            .anyMatch(item ->
                                    saved.getId().equals(item.getId()))
            );
        } finally {
            categoryRepository.deleteById(saved.getId());
        }
    }
}
