/**
 * ComprehensiveThreadDemo - Java多线程综合应用演示
 * 
 * 本类是Java多线程学习的综合演示，融合了所有线程创建方式
 * 包含继承Thread、实现Runnable、线程池等所有概念的实际应用
 * 展示真实项目中多线程系统的完整架构和最佳实践
 * 
 * 主要功能：
 * 1. 多线程概念理论整合
 * 2. 三种线程创建方式对比演示
 * 3. 真实应用场景模拟（电商订单系统）
 * 4. 性能监控与统计分析
 * 5. 线程生命周期完整管理
 * 6. 交互式学习和演示
 * 
 * @author Java Learning Tutorial
 * @version 1.0
 * @date 2024
 */

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.util.stream.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComprehensiveThreadDemo {
    
    /**
     * Java 8兼容的字符串工具方法
     */
    private static String padEnd(String str, int length, char padChar) {
        if (str.length() >= length) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }
    
    private static String repeat(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    // 全局计数器 - 用于统计系统性能
    private static final AtomicInteger totalOrdersProcessed = new AtomicInteger(0);
    private static final AtomicInteger totalPaymentsProcessed = new AtomicInteger(0);
    private static final AtomicInteger totalNotificationsSent = new AtomicInteger(0);
    private static final AtomicLong totalProcessingTime = new AtomicLong(0);
    private static final AtomicInteger systemStartTime = new AtomicInteger((int) System.currentTimeMillis());
    
    // 系统锁 - 用于模拟共享资源竞争
    private static final ReentrantLock inventoryLock = new ReentrantLock();
    private static final ReentrantLock paymentLock = new ReentrantLock();
    private static final ReentrantLock notificationLock = new ReentrantLock();
    
    // 订单状态枚举
    enum OrderStatus {
        PENDING, PROCESSING, PAID, SHIPPED, DELIVERED, CANCELLED
    }
    
    /**
     * 电商订单类 - 展示多线程系统中的数据模型
     */
    static class Order {
        private final int orderId;
        private final String customerName;
        private final List<String> products;
        private final double totalAmount;
        private volatile OrderStatus status;
        private volatile long startTime;
        private volatile long endTime;
        
        public Order(int orderId, String customerName, List<String> products, double totalAmount) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.products = new ArrayList<>(products);
            this.totalAmount = totalAmount;
            this.status = OrderStatus.PENDING;
            this.startTime = System.currentTimeMillis();
        }
        
        public int getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public List<String> getProducts() { return products; }
        public double getTotalAmount() { return totalAmount; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        
        @Override
        public String toString() {
            return String.format("订单#%d [%s] - %.2f元 - %s", 
                               orderId, customerName, totalAmount, status);
        }
    }
    
    // ==================== 继承Thread方式的实现 ====================
    
    /**
     * 订单处理线程 - 继承Thread方式
     * 展示最基本的线程创建方式
     */
    static class OrderProcessorThread extends Thread {
        private final Order order;
        private final List<Order> orderPool;
        
        public OrderProcessorThread(Order order, List<Order> orderPool) {
            super("OrderProcessor-" + order.getOrderId());
            this.order = order;
            this.orderPool = orderPool;
        }
        
        @Override
        public void run() {
            System.out.println("🛒 " + getName() + " 开始处理 " + order);
            
            try {
                // 步骤1: 验证订单
                order.setStatus(OrderStatus.PROCESSING);
                System.out.println("📋 " + getName() + " 正在验证订单...");
                Thread.sleep(500 + (int)(Math.random() * 500));
                
                // 步骤2: 检查库存（模拟资源竞争）
                inventoryLock.lock();
                try {
                    System.out.println("📦 " + getName() + " 正在检查库存...");
                    Thread.sleep(300);
                    System.out.println("✅ " + getName() + " 库存检查完成");
                } finally {
                    inventoryLock.unlock();
                }
                
                // 步骤3: 处理支付
                processPayment();
                
                // 步骤4: 更新订单状态
                order.setStatus(OrderStatus.SHIPPED);
                System.out.println("📦 " + getName() + " 订单处理完成");
                
                totalOrdersProcessed.incrementAndGet();
                
            } catch (InterruptedException e) {
                System.err.println("❌ " + getName() + " 处理被中断: " + e.getMessage());
                order.setStatus(OrderStatus.CANCELLED);
                Thread.currentThread().interrupt();
            }
        }
        
        private void processPayment() throws InterruptedException {
            paymentLock.lock();
            try {
                System.out.println("💳 " + getName() + " 正在处理支付...");
                Thread.sleep(400 + (int)(Math.random() * 600));
                order.setStatus(OrderStatus.PAID);
                System.out.println("💰 " + getName() + " 支付处理完成: " + order.getTotalAmount() + "元");
            } finally {
                paymentLock.unlock();
            }
        }
    }
    
    // ==================== 实现Runnable方式的实现 ====================
    
    /**
     * 支付处理线程 - 实现Runnable方式
     * 展示更灵活的线程创建方式
     */
    static class PaymentProcessorRunnable implements Runnable {
        private final Order order;
        private final CountDownLatch completionLatch;
        
        public PaymentProcessorRunnable(Order order, CountDownLatch completionLatch) {
            this.order = order;
            this.completionLatch = completionLatch;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("💳 支付处理开始: 订单#" + order.getOrderId());
                
                // 模拟支付流程
                String[] paymentSteps = {"验证用户", "检查余额", "执行扣款", "更新账户", "生成支付凭证"};
                
                for (int i = 0; i < paymentSteps.length; i++) {
                    System.out.println("  📝 支付步骤 " + (i+1) + ": " + paymentSteps[i]);
                    Thread.sleep(200 + (int)(Math.random() * 300));
                    
                    // 显示进度
                    int progress = (i + 1) * 100 / paymentSteps.length;
                    if ((i + 1) % 2 == 0 || i == paymentSteps.length - 1) {
                        System.out.println("    📊 支付进度: " + progress + "%");
                    }
                }
                
                totalPaymentsProcessed.incrementAndGet();
                System.out.println("✅ 支付处理完成: 订单#" + order.getOrderId());
                
            } catch (InterruptedException e) {
                System.err.println("❌ 支付处理被中断: 订单#" + order.getOrderId());
                Thread.currentThread().interrupt();
            } finally {
                if (completionLatch != null) {
                    completionLatch.countDown();
                }
            }
        }
    }
    
    /**
     * 通知服务线程 - 实现Runnable方式
     * 展示匿名内部类和Lambda表达式的使用
     */
    static class NotificationServiceRunnable implements Runnable {
        private final Order order;
        private final String notificationType;
        
        public NotificationServiceRunnable(Order order, String notificationType) {
            this.order = order;
            this.notificationType = notificationType;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("📧 " + notificationType + " 发送开始: " + order.getCustomerName());
                
                // 模拟发送通知
                Thread.sleep(300 + (int)(Math.random() * 400));
                
                // 根据通知类型模拟不同的发送效果
                switch (notificationType) {
                    case "邮件通知":
                        simulateEmailSending();
                        break;
                    case "短信通知":
                        simulateSMS();
                        break;
                    case "推送通知":
                        simulatePushNotification();
                        break;
                }
                
                totalNotificationsSent.incrementAndGet();
                System.out.println("✅ " + notificationType + " 发送完成: " + order.getCustomerName());
                
            } catch (InterruptedException e) {
                System.err.println("❌ " + notificationType + " 发送被中断");
                Thread.currentThread().interrupt();
            }
        }
        
        private void simulateEmailSending() throws InterruptedException {
            System.out.println("    📧 正在连接邮件服务器...");
            Thread.sleep(100);
            System.out.println("    📨 正在发送邮件内容...");
            Thread.sleep(150);
            System.out.println("    ✅ 邮件发送成功");
        }
        
        private void simulateSMS() throws InterruptedException {
            System.out.println("    📱 正在连接短信网关...");
            Thread.sleep(80);
            System.out.println("    📲 正在发送短信内容...");
            Thread.sleep(120);
            System.out.println("    ✅ 短信发送成功");
        }
        
        private void simulatePushNotification() throws InterruptedException {
            System.out.println("    🔔 正在连接推送服务器...");
            Thread.sleep(60);
            System.out.println("    📡 正在发送推送消息...");
            Thread.sleep(100);
            System.out.println("    ✅ 推送消息发送成功");
        }
    }
    
    // ==================== 线程池方式的实现 ====================
    
    /**
     * 库存管理服务 - 线程池方式
     * 展示线程池在批量任务处理中的优势
     */
    static class InventoryManagementService {
        private final ThreadPoolExecutor inventoryPool;
        
        public InventoryManagementService() {
            inventoryPool = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                r -> new Thread(r, "InventoryPool-Worker"),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }
        
        public void processInventoryUpdate(Order order) {
            inventoryPool.submit(() -> {
                try {
                    System.out.println("📦 库存更新开始: 订单#" + order.getOrderId());
                    
                    // 模拟库存检查和更新
                    for (String product : order.getProducts()) {
                        System.out.println("  📋 检查库存: " + product);
                        Thread.sleep(200);
                        System.out.println("  ✅ " + product + " 库存充足");
                    }
                    
                    System.out.println("✅ 库存更新完成: 订单#" + order.getOrderId());
                    
                } catch (InterruptedException e) {
                    System.err.println("❌ 库存更新被中断");
                }
            });
        }
        
        public void shutdown() {
            inventoryPool.shutdown();
            try {
                inventoryPool.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                inventoryPool.shutdownNow();
            }
        }
    }
    
    /**
     * 日志记录服务 - 线程池方式
     * 展示线程池处理日志和监控任务
     */
    static class LoggingService {
        private final ScheduledExecutorService scheduledLogger = 
            Executors.newScheduledThreadPool(1);
        
        public void startPerformanceLogging() {
            // 每3秒记录一次系统性能
            scheduledLogger.scheduleAtFixedRate(this::logSystemPerformance, 3, 3, TimeUnit.SECONDS);
        }
        
        private void logSystemPerformance() {
            long currentTime = System.currentTimeMillis();
            long runningTime = (currentTime - systemStartTime.get()) / 1000;
            
            System.out.println("\n📊 === 系统性能日志 (运行" + runningTime + "秒) ===");
            System.out.println("  🛒 总订单处理数: " + totalOrdersProcessed.get());
            System.out.println("  💳 总支付处理数: " + totalPaymentsProcessed.get());
            System.out.println("  📧 总通知发送数: " + totalNotificationsSent.get());
            System.out.println("  ⏱️ 系统运行时间: " + runningTime + "秒");
            System.out.println("  📈 平均每秒处理订单: " + 
                             (runningTime > 0 ? totalOrdersProcessed.get() / runningTime : 0));
            System.out.println("================================================\n");
        }
        
        public void stopLogging() {
            scheduledLogger.shutdown();
            try {
                scheduledLogger.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                scheduledLogger.shutdownNow();
            }
        }
    }
    
    /**
     * 系统监控器 - 展示线程池监控功能
     */
    static class SystemMonitor {
        private final ThreadPoolExecutor monitoringPool;
        
        public SystemMonitor() {
            monitoringPool = new ThreadPoolExecutor(
                1, 2, 30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(50)
            );
        }
        
        public void monitorOrderProcessing(List<Order> orders) {
            monitoringPool.submit(() -> {
                try {
                    System.out.println("\n🔍 开始监控系统状态...");
                    
                    // 模拟监控过程
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(1000);
                        
                        int activeOrders = (int) orders.stream()
                            .filter(o -> o.getStatus() == OrderStatus.PROCESSING || 
                                        o.getStatus() == OrderStatus.PAID)
                            .count();
                        
                        System.out.println("📊 监控点 " + (i+1) + ": 活跃订单 " + activeOrders + 
                                         " 个，已完成 " + totalOrdersProcessed.get() + " 个");
                    }
                    
                    System.out.println("✅ 监控任务完成\n");
                    
                } catch (InterruptedException e) {
                    System.err.println("❌ 监控系统被中断");
                }
            });
        }
        
        public void shutdown() {
            monitoringPool.shutdown();
            try {
                monitoringPool.awaitTermination(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                monitoringPool.shutdownNow();
            }
        }
    }
    
    // ==================== 主方法和综合演示 ====================
    
    public static void main(String[] args) {
        System.out.println(repeat("=", 80));
        System.out.println("🎓 ComprehensiveThreadDemo - Java多线程综合应用演示");
        System.out.println("模拟真实电商订单系统的完整多线程架构");
        System.out.println(repeat("=", 80));
        
        try {
            // 显示系统开始信息
            showSystemStartInfo();
            
            // 演示1: 基础概念展示
            demonstrateBasicConcepts();
            
            // 演示2: 三种线程创建方式对比
            demonstrateThreadCreationMethods();
            
            // 演示3: 真实应用场景模拟
            demonstrateEcommerceSystem();
            
            // 演示4: 性能监控和统计分析
            demonstratePerformanceMonitoring();
            
            // 演示5: 最佳实践和总结
            demonstrateBestPractices();
            
        } catch (Exception e) {
            System.err.println("❌ 系统运行出现错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\n🎉 综合演示完成！");
            printFinalSummary();
        }
    }
    
    /**
     * 显示系统开始信息
     */
    private static void showSystemStartInfo() {
        System.out.println("\n📋 系统启动信息:");
        System.out.println("  🖥️ 操作系统: " + System.getProperty("os.name"));
        System.out.println("  ☕ Java版本: " + System.getProperty("java.version"));
        System.out.println("  💻 CPU核心数: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  📊 最大内存: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB");
        System.out.println("  ⏰ 启动时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * 演示1: 基础概念展示
     */
    private static void demonstrateBasicConcepts() {
        System.out.println("\n" + padEnd("🔸 演示1: 多线程基础概念回顾", 70, ' '));
        System.out.println(repeat("-", 70));
        
        System.out.println("📚 进程 vs 线程:");
        System.out.println("  • 进程: 程序执行的基本单位，拥有独立内存空间");
        System.out.println("  • 线程: CPU调度的基本单位，同一进程内共享内存");
        
        System.out.println("\n🏗️ 线程创建方式对比:");
        System.out.println("  1️⃣ 继承Thread: 代码简单，但类无法再继承其他类");
        System.out.println("  2️⃣ 实现Runnable: 更灵活，任务与线程分离");
        System.out.println("  3️⃣ 线程池: 高效管理，适合大量并发任务");
        
        System.out.println("\n⚡ 并发优势:");
        System.out.println("  • 提高系统吞吐量");
        System.out.println("  • 提升用户体验");
        System.out.println("  • 充分利用多核CPU");
        System.out.println("  • 异步处理耗时任务");
    }
    
    /**
     * 演示2: 三种线程创建方式对比
     */
    private static void demonstrateThreadCreationMethods() {
        System.out.println("\n" + padEnd("🔸 演示2: 三种线程创建方式实际对比", 70, ' '));
        System.out.println(repeat("-", 70));
        
        // 创建测试订单
        List<Order> testOrders = createTestOrders(6);
        
        System.out.println("🧪 创建" + testOrders.size() + "个测试订单用于对比演示");
        testOrders.forEach(order -> System.out.println("  📝 " + order));
        
        // 方式1: 继承Thread
        demonstrateThreadExtends(testOrders.subList(0, 2));
        
        // 方式2: 实现Runnable
        demonstrateRunnable(testOrders.subList(2, 4));
        
        // 方式3: 线程池
        demonstrateThreadPool(testOrders.subList(4, 6));
    }
    
    /**
     * 演示Thread继承方式
     */
    private static void demonstrateThreadExtends(List<Order> orders) {
        System.out.println("\n🔹 方式1: 继承Thread方式");
        
        List<Thread> threads = new ArrayList<>();
        for (Order order : orders) {
            Thread thread = new OrderProcessorThread(order, new ArrayList<>());
            threads.add(thread);
        }
        
        long startTime = System.currentTimeMillis();
        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try { thread.join(); } catch (InterruptedException e) {}
        });
        long endTime = System.currentTimeMillis();
        
        System.out.println("✅ 继承Thread方式完成，耗时: " + (endTime - startTime) + "ms");
    }
    
    /**
     * 演示Runnable实现方式
     */
    private static void demonstrateRunnable(List<Order> orders) {
        System.out.println("\n🔹 方式2: 实现Runnable方式");
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<?>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        for (Order order : orders) {
            CountDownLatch latch = new CountDownLatch(1);
            Runnable task = new PaymentProcessorRunnable(order, latch);
            futures.add(executor.submit(task));
        }
        
        // 等待所有任务完成
        futures.forEach(future -> {
            try { future.get(); } catch (Exception e) {}
        });
        executor.shutdown();
        long endTime = System.currentTimeMillis();
        
        System.out.println("✅ 实现Runnable方式完成，耗时: " + (endTime - startTime) + "ms");
    }
    
    /**
     * 演示线程池方式
     */
    private static void demonstrateThreadPool(List<Order> orders) {
        System.out.println("\n🔹 方式3: 线程池方式");
        
        InventoryManagementService inventoryService = new InventoryManagementService();
        
        long startTime = System.currentTimeMillis();
        for (Order order : orders) {
            inventoryService.processInventoryUpdate(order);
        }
        inventoryService.shutdown();
        long endTime = System.currentTimeMillis();
        
        System.out.println("✅ 线程池方式完成，耗时: " + (endTime - startTime) + "ms");
    }
    
    /**
     * 演示3: 真实电商系统模拟
     */
    private static void demonstrateEcommerceSystem() {
        System.out.println("\n" + padEnd("🔸 演示3: 真实电商订单系统模拟", 70, ' '));
        System.out.println(repeat("-", 70));
        
        // 初始化系统组件
        InventoryManagementService inventoryService = new InventoryManagementService();
        LoggingService loggingService = new LoggingService();
        SystemMonitor systemMonitor = new SystemMonitor();
        
        // 创建测试订单
        List<Order> orders = createTestOrders(10);
        
        System.out.println("🏪 电商系统启动，处理" + orders.size() + "个订单...");
        
        // 启动性能日志记录
        loggingService.startPerformanceLogging();
        
        // 启动系统监控
        systemMonitor.monitorOrderProcessing(orders);
        
        long systemStartTime = System.currentTimeMillis();
        
        // 处理每个订单
        for (Order order : orders) {
            System.out.println("\n🛍️ ===== 开始处理订单 #" + order.getOrderId() + " =====");
            
            try {
                // 订单处理（继承Thread方式）
                OrderProcessorThread processor = new OrderProcessorThread(order, orders);
                processor.start();
                processor.join();
                
                // 支付处理（实现Runnable方式）
                CountDownLatch paymentLatch = new CountDownLatch(1);
                ExecutorService paymentExecutor = Executors.newSingleThreadExecutor();
                paymentExecutor.submit(new PaymentProcessorRunnable(order, paymentLatch));
                paymentLatch.await();
                paymentExecutor.shutdown();
                
                // 通知发送（匿名Runnable和Lambda）
                sendNotifications(order);
                
                // 库存管理（线程池方式）
                inventoryService.processInventoryUpdate(order);
                
                order.setEndTime(System.currentTimeMillis());
                long processingTime = order.getEndTime() - order.getStartTime();
                totalProcessingTime.addAndGet(processingTime);
                
                System.out.println("✅ 订单 #" + order.getOrderId() + " 处理完成，耗时: " + processingTime + "ms");
                
            } catch (InterruptedException e) {
                System.err.println("❌ 订单 #" + order.getOrderId() + " 处理被中断");
                order.setStatus(OrderStatus.CANCELLED);
            }
        }
        
        // 等待库存服务完成
        inventoryService.shutdown();
        
        // 停止日志记录
        loggingService.stopLogging();
        
        // 停止监控
        systemMonitor.shutdown();
        
        long systemEndTime = System.currentTimeMillis();
        
        System.out.println("\n🏁 电商系统处理完成，总耗时: " + (systemEndTime - systemStartTime) + "ms");
        printOrderStatistics(orders);
    }
    
    /**
     * 发送通知（演示匿名类和Lambda的使用）
     */
    private static void sendNotifications(Order order) throws InterruptedException {
        ExecutorService notificationExecutor = Executors.newFixedThreadPool(3);
        
        // 使用匿名内部类
        notificationExecutor.submit(new NotificationServiceRunnable(order, "邮件通知"));
        
        // 使用Lambda表达式
        notificationExecutor.submit(() -> {
            try {
                System.out.println("📱 手机推送开始: " + order.getCustomerName());
                Thread.sleep(200);
                System.out.println("✅ 手机推送完成: " + order.getCustomerName());
                totalNotificationsSent.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 使用方法引用
        notificationExecutor.submit(createSMSTask(order));
        
        notificationExecutor.shutdown();
        notificationExecutor.awaitTermination(10, TimeUnit.SECONDS);
    }
    
    /**
     * 创建短信任务的方法
     */
    private static Runnable createSMSTask(Order order) {
        return () -> {
            try {
                System.out.println("📲 短信发送开始: " + order.getCustomerName());
                Thread.sleep(150);
                System.out.println("✅ 短信发送完成: " + order.getCustomerName());
                totalNotificationsSent.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
    
    /**
     * 演示4: 性能监控
     */
    private static void demonstratePerformanceMonitoring() {
        System.out.println("\n" + padEnd("🔸 演示4: 性能监控与统计分析", 70, ' '));
        System.out.println(repeat("-", 70));
        
        System.out.println("📊 系统性能指标:");
        System.out.println("  🛒 订单处理量: " + totalOrdersProcessed.get() + " 个");
        System.out.println("  💳 支付处理量: " + totalPaymentsProcessed.get() + " 个");
        System.out.println("  📧 通知发送量: " + totalNotificationsSent.get() + " 条");
        
        long totalTime = totalProcessingTime.get();
        double avgProcessingTime = totalOrdersProcessed.get() > 0 ? 
            (double) totalTime / totalOrdersProcessed.get() : 0;
        
        System.out.println("  ⏱️ 总处理时间: " + totalTime + "ms");
        System.out.println("  📈 平均处理时间: " + String.format("%.2f", avgProcessingTime) + "ms");
        
        double throughput = totalOrdersProcessed.get() / (totalTime / 1000.0);
        System.out.println("  🚀 系统吞吐量: " + String.format("%.2f", throughput) + " 订单/秒");
    }
    
    /**
     * 演示5: 最佳实践
     */
    private static void demonstrateBestPractices() {
        System.out.println("\n" + padEnd("🔸 演示5: 多线程编程最佳实践", 70, ' '));
        System.out.println(repeat("-", 70));
        
        System.out.println("🎯 选择合适的线程创建方式:");
        System.out.println("  • 简单任务 → 优先考虑Lambda表达式");
        System.out.println("  • 复杂逻辑 → 使用Runnable接口");
        System.out.println("  • 简单继承 → 继承Thread类（不推荐）");
        System.out.println("  • 批量任务 → 使用线程池");
        
        System.out.println("\n💡 性能优化建议:");
        System.out.println("  • CPU密集型任务: 线程数 = CPU核心数");
        System.out.println("  • I/O密集型任务: 线程数 = CPU核心数 × 2");
        System.out.println("  • 使用线程池避免频繁创建/销毁线程");
        System.out.println("  • 合理设置队列大小防止OOM");
        
        System.out.println("\n⚠️ 常见陷阱和解决方案:");
        System.out.println("  • 死锁 → 使用超时机制和锁顺序");
        System.out.println("  • 内存泄漏 → 正确关闭线程池");
        System.out.println("  • 线程安全 → 使用同步机制或并发集合");
        System.out.println("  • 资源竞争 → 合理使用锁和并发工具");
        
        System.out.println("\n🛠️ 调试和监控技巧:");
        System.out.println("  • 使用线程ID和命名");
        System.out.println("  • 添加详细的日志记录");
        System.out.println("  • 监控线程状态和资源使用");
        System.out.println("  • 使用性能分析工具");
    }
    
    /**
     * 创建测试订单
     */
    private static List<Order> createTestOrders(int count) {
        List<Order> orders = new ArrayList<>();
        String[] customers = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "王十二"};
        String[][] productLists = {
            {"iPhone 15", "AirPods Pro"},
            {"MacBook Pro", "iPad Air"},
            {"小米13", "小米耳机"},
            {"华为P60", "华为手表"},
            {"Switch游戏机", "塞尔达传说"},
            {"PS5主机", "FIFA 24"},
            {"戴尔笔记本", "戴尔显示器"},
            {"联想台式机", "机械键盘"},
            {"索尼相机", "索尼镜头"},
            {"Apple Watch", "iPad Pro"}
        };
        
        for (int i = 0; i < count; i++) {
            String customer = customers[i % customers.length];
            String[] products = productLists[i % productLists.length];
            double amount = 1000 + Math.random() * 9000;
            
            List<String> productList = Arrays.asList(products);
            Order order = new Order(i + 1, customer, productList, amount);
            orders.add(order);
        }
        
        return orders;
    }
    
    /**
     * 打印订单统计信息
     */
    private static void printOrderStatistics(List<Order> orders) {
        System.out.println("\n📊 订单处理统计:");
        
        Map<OrderStatus, Long> statusCount = orders.stream()
            .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        
        statusCount.forEach((status, count) -> 
            System.out.println("  " + status + ": " + count + " 个"));
        
        double avgTime = orders.stream()
            .mapToLong(o -> o.getEndTime() - o.getStartTime())
            .average()
            .orElse(0);
        
        System.out.println("  平均处理时间: " + String.format("%.2f", avgTime) + "ms");
    }
    
    /**
     * 打印最终总结
     */
    private static void printFinalSummary() {
        System.out.println("\n" + padEnd("🎉 ComprehensiveThreadDemo 演示总结", 70, ' '));
        System.out.println(repeat("-", 70));
        
        System.out.println("✅ 完成的功能演示:");
        System.out.println("  1. 📚 多线程理论知识整合");
        System.out.println("  2. 🔧 三种线程创建方式对比");
        System.out.println("  3. 🏪 真实电商系统模拟");
        System.out.println("  4. 📊 性能监控和统计");
        System.out.println("  5. 💡 最佳实践指南");
        
        System.out.println("\n🎯 学习要点:");
        System.out.println("  • 理解进程与线程的区别");
        System.out.println("  • 掌握三种线程创建方式");
        System.out.println("  • 学会使用线程池管理并发任务");
        System.out.println("  • 了解线程安全和同步机制");
        System.out.println("  • 实践真实项目的多线程架构");
        
        System.out.println("\n📈 性能表现:");
        System.out.println("  • 处理订单: " + totalOrdersProcessed.get() + " 个");
        System.out.println("  • 处理支付: " + totalPaymentsProcessed.get() + " 个");
        System.out.println("  • 发送通知: " + totalNotificationsSent.get() + " 条");
        
        System.out.println("\n🚀 持续改进建议:");
        System.out.println("  • 添加更多的错误处理机制");
        System.out.println("  • 实现更复杂的业务逻辑");
        System.out.println("  • 使用更高级的并发工具");
        System.out.println("  • 集成数据库和外部服务");
        
        System.out.println("\n" + repeat("=", 70));
        System.out.println("🎓 感谢使用 Java多线程综合学习系统！");
        System.out.println("希望这个演示能帮助您深入理解多线程编程");
        System.out.println(repeat("=", 70));
    }
}