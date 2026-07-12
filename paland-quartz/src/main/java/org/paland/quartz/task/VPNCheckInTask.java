package org.paland.quartz.task;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Component;

@Component("vpnCheckInTask")
public class VPNCheckInTask {
    String pageUrl = "https://ikuuu.one/";
    String loginUrl1 = "https://ikuuu.win/";
    String loginUrl2 = "https://ikuuu.fyi/";

    public void parseConfusedHtml() {
        System.out.println(">>> 【PaLand 定时任务】正在启动内核浏览器处理混淆网页... ");
        String pageUrl = "https://ikuuu.one/";

        // 1. 创建 Playwright 实例
        try (Playwright playwright = Playwright.create()) {
            // 2. 启动无头浏览器（headless = true 表示不在桌面弹出浏览器窗口，静默运行）
            try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                // 3. 访问目标网址
                page.navigate(pageUrl);

                System.out.println(">>> 【PaLand 提示】网页已加载，正在等待内部 JS 脚本执行并解密渲染...");
                // 4. 关键：给网页留出一点解密渲染的时间（比如延迟 3-5 秒）
                page.waitForTimeout(4000);

                // 5. 使用元素选择器获取页面上所有的 <a> 标签
                // Playwright 的 locator 会实时抓取浏览器当前运行时的真实 DOM 树（即解密后的网页）
                Locator links = page.locator("a");
                int totalLinks = links.count();

                System.out.println(">>> 【PaLand 成功穿透混淆】检测到渲染后的 <a> 标签总数: " + totalLinks);
                System.out.println(">>> 开始提取前 2 个有效地址：");

                int validCount = 1;
                for (int i = 0; i < totalLinks; i++) {
                    Locator link = links.nth(i);

                    // 获取解密后真实的 href 属性和文本
                    String targetUrl = link.getAttribute("href");
                    String linkText = link.innerText();

                    // 过滤掉无效链接
                    if (targetUrl == null || targetUrl.trim().isEmpty() || targetUrl.startsWith("javascript:")) {
                        continue;
                    }

                    // 打印提取出来的结果
                    System.out.println("    -> 目标地址 " + validCount + " [" + linkText.trim() + "]: " + targetUrl);
                    validCount++;

                    // 满足 2 个后立刻退出，节约资源
                    if (validCount > 2) {
                        break;
                    }
                }

                if (validCount == 1) {
                    System.out.println(">>> 【PaLand 业务警告】虽然穿透了混淆，但未在页面中找到符合条件的 <a> 标签！");
                }

            }
        } catch (Exception e) {
            System.err.println(">>> 【PaLand 浏览器运行异常】: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void pureLoginTest() {
        System.out.println(">>> 【PaLand 测试】启动纯净版浏览器，不带任何伪装...");
        String loginUrl = "https://ikuuu.win/";

        // 1. 初始化 Playwright
        try (Playwright playwright = Playwright.create()) {
            // 2. setHeadless(false) 极其关键！它会弹出一个真实的浏览器界面，方便你肉眼观察
            try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false))) {
                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                // 3. 打开登录页
                page.navigate(loginUrl);
                System.out.println(">>> 页面已打开，等待加载...");
                page.waitForTimeout(2000); // 稳妥起见，等 2 秒

                // 4. 输入账号密码
                // 注意：请将这里的 "input[type='text']" 等换成你网页中实际的输入框定位器
                System.out.println(">>> 正在输入账号密码...");
                page.locator("input[type='email']").first().fill("167510385@qq.com");
                page.locator("input[type='password']").first().fill("cj970720.");

                // 5. 点击人机验证
                // 注意：请将 ".verify-button" 换成那个人机验证按钮的实际 class、id 或文本
                System.out.println(">>> 正在点击人机验证按钮...");
                page.locator("embed-captcha").click();

                // 给它 3 秒时间让它完成那个“秘密通信”的验证流程
                page.waitForTimeout(3000);

                // 6. 点击登录按钮
                System.out.println(">>> 正在点击登录按钮...");
                // 如果登录按钮是个普通按钮，可以用文本匹配，比如 page.locator("text=登录")
                page.locator("button[type='submit']").click();

                // 7. 等待登录后的页面跳转
                System.out.println(">>> 等待登录跳转中...");
                page.waitForTimeout(5000); // 先死等 5 秒，看看浏览器里有没有成功进到后台

                // 8. 既然进入了目标网址（内页），且内页没有混淆和 debugger
                // 我们直接再次提取当前内页里的那两个 a 标签
                System.out.println(">>> 【开始提取内页目标地址】...");
                Locator links = page.locator("a");
                int totalLinks = links.count();
                int validCount = 1;

                for (int i = 0; i < totalLinks; i++) {
                    Locator link = links.nth(i);
                    String targetUrl = link.getAttribute("href");
                    String linkText = link.innerText();

                    if (targetUrl == null || targetUrl.trim().isEmpty() || targetUrl.startsWith("javascript:")) {
                        continue;
                    }

                    System.out.println("    -> 提取成功 [" + linkText.trim() + "]: " + targetUrl);
                    validCount++;
                    if (validCount > 2) {
                        break;
                    }
                }

                // 为了让你在控制台看清楚，让浏览器多存活 10 秒再关闭
                System.out.println(">>> 测试完毕，10秒后自动关闭浏览器...");
                page.waitForTimeout(10000);

            }
        } catch (Exception e) {
            System.err.println(">>> 【PaLand 测试失败】发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
