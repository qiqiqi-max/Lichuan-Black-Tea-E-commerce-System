package com.lichuan.tea;

import com.lichuan.tea.entity.FarmerProfile;
import com.lichuan.tea.entity.FarmerStory;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.FarmerProfileRepository;
import com.lichuan.tea.repository.FarmerStoryRepository;
import com.lichuan.tea.repository.ProductRepository;
import com.lichuan.tea.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            ProductRepository productRepository,
            FarmerProfileRepository farmerProfileRepository,
            FarmerStoryRepository farmerStoryRepository) {
        return args -> {
            ensureUser(userRepository, "admin", "123456", "ADMIN", "管理员");
            ensureUser(userRepository, "user", "123456", "USER", "测试用户");
            User farmerUser = ensureUser(userRepository, "farmer", "123456", "FARMER", "王大伯");

            FarmerProfile farmerProfile = farmerProfileRepository.findByUserId(farmerUser.getId())
                    .orElseGet(() -> {
                        FarmerProfile profile = new FarmerProfile();
                        profile.setUser(farmerUser);
                        profile.setRealName("王大伯");
                        profile.setAddress("利川市毛坝镇");
                        profile.setOrigin("利川市毛坝镇");
                        profile.setStory("扎根茶山三十年，坚持手工制茶。");
                        return farmerProfileRepository.save(profile);
                    });

            normalizeEnglishSeedData(farmerProfile, farmerProfileRepository, productRepository);

            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setName("利川红·冷后浑（特级）");
                p1.setOrigin("利川市毛坝镇");
                p1.setFarmer(farmerProfile);
                p1.setPrice(new BigDecimal("268.00"));
                p1.setCategory("冷后浑");
                p1.setDescription("高山有机红茶，冷后浑明显，香气高扬，回甘持久。");
                p1.setStory("每年春茶时节，茶农在云雾缭绕的高山茶园中手工采摘，只为保留最鲜活的一口茶香。");
                p1.setStock(100);
                p1.setSales(0);
                p1.setCoverImg("/images/products/product1.jpg");
                productRepository.save(p1);

                Product p2 = new Product();
                p2.setName("利川红·玛瑙红（袋泡）");
                p2.setOrigin("利川市沙溪乡");
                p2.setFarmer(farmerProfile);
                p2.setPrice(new BigDecimal("58.00"));
                p2.setCategory("玛瑙红");
                p2.setDescription("办公居家便捷袋泡装，汤色红亮，口感醇厚。");
                p2.setStory("从采摘到拼配再到分装，每一步都严格把控，让日常饮茶也能喝到地道利川红。");
                p2.setStock(200);
                p2.setSales(0);
                p2.setCoverImg("/images/products/product2.jpg");
                productRepository.save(p2);

                Product p3 = new Product();
                p3.setName("恩施玉露·利川红礼盒");
                p3.setOrigin("利川市忠路镇");
                p3.setFarmer(farmerProfile);
                p3.setPrice(new BigDecimal("388.00"));
                p3.setCategory("礼盒");
                p3.setDescription("高端礼盒，送礼自饮皆宜，兼具品质与文化感。");
                p3.setStory("礼盒茶精选当季优质原料，既有山野清香，也有助农温度，适合节日与商务场景。");
                p3.setStock(50);
                p3.setSales(0);
                p3.setCoverImg("/images/products/product3.jpg");
                productRepository.save(p3);
            }

            if (farmerStoryRepository.count() == 0) {
                FarmerStory s1 = new FarmerStory();
                s1.setFarmerName("陈大伯");
                s1.setRegion("利川市沙溪乡");
                s1.setImageUrl("/images/farmers/farmer1.1.jpg");
                s1.setTagline("一生炒茶的沙溪老茶人");
                s1.setSummary("陈大伯扎根茶山近五十年，守着一口铁锅和一双老手，把山里的清香炒进每一片茶叶。");
                s1.setContent("陈大伯出生在利川市沙溪乡的茶农家庭，十几岁就跟着父辈学采茶、炒茶。"
                        + "几十年来，他坚持手工制茶，尤其看重火候与揉捻，常说“茶要慢慢养，急不得”。\n"
                        + "每到春茶季，他总是天不亮上山，挑选一芽一叶的鲜叶，回到作坊后再细致分拣。"
                        + "他希望年轻一代不仅学会技术，也能学会对土地与茶树的敬畏。\n"
                        + "在平台上线后，陈大伯的茶被更多消费者看见，他也更有动力把这门手艺传下去。");
                s1.setSortOrder(1);
                s1.setStatus(true);
                s1.setCreatedTime(LocalDateTime.now());
                s1.setUpdatedTime(LocalDateTime.now());
                farmerStoryRepository.save(s1);

                FarmerStory s2 = new FarmerStory();
                s2.setFarmerName("李大姐");
                s2.setRegion("利川市毛坝镇");
                s2.setImageUrl("/images/farmers/farmer2.1.jpg");
                s2.setTagline("把茶园当作家园来守护");
                s2.setSummary("李大姐坚持生态种植，用心照看每一片茶园，让更多人喝到安心、真实的利川红茶。");
                s2.setContent("李大姐在毛坝镇经营家庭茶园二十多年，始终坚持生态种植，减少农药化肥使用。"
                        + "她相信好茶首先来自好土壤、好山水。\n"
                        + "为了让茶叶品质稳定，她和家人把采摘、摊晾、发酵到干燥每一个环节都做了详细记录，"
                        + "并主动向周边茶农分享经验。\n"
                        + "她说“卖茶不只是在卖一件商品，更是在把家乡的味道和心意送出去”。");
                s2.setSortOrder(2);
                s2.setStatus(true);
                s2.setCreatedTime(LocalDateTime.now());
                s2.setUpdatedTime(LocalDateTime.now());
                farmerStoryRepository.save(s2);
            }
        };
    }

    private User ensureUser(
            UserRepository userRepository,
            String username,
            String password,
            String role,
            String nickname) {
        User existing = userRepository.findByUsername(username);
        if (existing != null) {
            return existing;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    private void normalizeEnglishSeedData(
            FarmerProfile farmerProfile,
            FarmerProfileRepository farmerProfileRepository,
            ProductRepository productRepository) {
        if (farmerProfile != null) {
            boolean profileChanged = false;
            if ("Farmer Wang".equals(farmerProfile.getRealName())) {
                farmerProfile.setRealName("王大伯");
                profileChanged = true;
            }
            if ("Maoba Town, Lichuan".equals(farmerProfile.getAddress())) {
                farmerProfile.setAddress("利川市毛坝镇");
                profileChanged = true;
            }
            if ("Maoba Town, Lichuan".equals(farmerProfile.getOrigin())) {
                farmerProfile.setOrigin("利川市毛坝镇");
                profileChanged = true;
            }
            if ("Tea farmer with 30 years of handcraft tea experience.".equals(farmerProfile.getStory())) {
                farmerProfile.setStory("扎根茶山三十年，坚持手工制茶。");
                profileChanged = true;
            }
            if (profileChanged) {
                farmerProfileRepository.save(farmerProfile);
            }
        }

        productRepository.findAll().forEach(product -> {
            boolean changed = false;
            if ("Lichuan Black Tea - Cold Haze (Premium)".equals(product.getName())) {
                product.setName("利川红·冷后浑（特级）");
                product.setOrigin("利川市毛坝镇");
                product.setCategory("冷后浑");
                product.setDescription("高山有机红茶，冷后浑明显，香气高扬，回甘持久。");
                product.setStory("每年春茶时节，茶农在云雾缭绕的高山茶园中手工采摘，只为保留最鲜活的一口茶香。");
                changed = true;
            } else if ("Lichuan Black Tea - Agate Red (Tea Bag)".equals(product.getName())) {
                product.setName("利川红·玛瑙红（袋泡）");
                product.setOrigin("利川市沙溪乡");
                product.setCategory("玛瑙红");
                product.setDescription("办公居家便捷袋泡装，汤色红亮，口感醇厚。");
                product.setStory("从采摘到拼配再到分装，每一步都严格把控，让日常饮茶也能喝到地道利川红。");
                changed = true;
            } else if ("Enshi Yulu x Lichuan Black Tea Gift Box".equals(product.getName())) {
                product.setName("恩施玉露·利川红礼盒");
                product.setOrigin("利川市忠路镇");
                product.setCategory("礼盒");
                product.setDescription("高端礼盒，送礼自饮皆宜，兼具品质与文化感。");
                product.setStory("礼盒茶精选当季优质原料，既有山野清香，也有助农温度，适合节日与商务场景。");
                changed = true;
            }
            if (changed) {
                productRepository.save(product);
            }
        });
    }
}
