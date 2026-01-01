package com.lichuan.tea;

import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.ProductRepository;
import com.lichuan.tea.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, ProductRepository productRepository) {
        return args -> {
            // Init Users
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("123456");
                admin.setRole("ADMIN");
                admin.setNickname("管理员");
                userRepository.save(admin);

                User user = new User();
                user.setUsername("user");
                user.setPassword("123456");
                user.setRole("USER");
                user.setNickname("测试用户");
                userRepository.save(user);
            }

            // Init Products
            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setName("利川红·冷后浑(特级)");
                p1.setOrigin("利川市毛坝镇");
                p1.setFarmerName("王大伯");
                p1.setPrice(new BigDecimal("268.0"));
                p1.setCategory("冷后浑");
                p1.setDescription("高山有机茶，冷后浑现象明显，口感鲜爽。");
                p1.setStock(100);
                p1.setCoverImg("https://placehold.co/400x400?text=Lichuan+Red+1");
                productRepository.save(p1);

                Product p2 = new Product();
                p2.setName("利川红·玛瑙红(袋泡)");
                p2.setOrigin("利川市沙溪乡");
                p2.setFarmerName("沙溪茶叶合作社");
                p2.setPrice(new BigDecimal("58.0"));
                p2.setCategory("玛瑙红");
                p2.setDescription("办公居家首选，汤色红艳明亮。");
                p2.setStock(200);
                p2.setCoverImg("https://placehold.co/400x400?text=Lichuan+Red+2");
                productRepository.save(p2);

                Product p3 = new Product();
                p3.setName("恩施玉露·利川红礼盒");
                p3.setOrigin("利川市忠路镇");
                p3.setFarmerName("李大姐");
                p3.setPrice(new BigDecimal("388.0"));
                p3.setCategory("礼盒");
                p3.setDescription("高端大气，送礼佳品。");
                p3.setStock(50);
                p3.setCoverImg("https://placehold.co/400x400?text=Gift+Box");
                productRepository.save(p3);
            }
        };
    }
}
