/**
 * ThreadPoolDemo - 演示线程池的高级应用
 * 
 * 本类展示了Java多线程编程的高级方式：使用线程池
 * 包含固定线程池、缓存线程池、单线程池、调度线程池等多种类型
 * 展示线程池的优势、配置参数、监控方法以及实际应用场景
 * 
 * @author Java Learning Tutorial
 * @version 1.0
 * @date 2024
 */

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadPoolDemo {
    
    // 任务计数器，用于统计任务执行情况
    private static final AtomicInteger taskCounter = new AtomicInteger(0);
    private static final AtomicInteger completedCounter = new AtomicInteger(0);
    private static final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    /**
     * 计算密集型任务类 - 模拟CPU密集型工作
     * 展示线程池处理计算任务的效率
     */
    static class ComputationTask implements Runnable {
        private final String taskName;
        private final int complexityLevel; // 复杂度级别
        private final long startTime;
        
        public ComputationTask(String taskName, int complexityLevel) {
            this.taskName = taskName;
            this.complexityLevel = complexityLevel;
            this.startTime = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            int taskId = taskCounter.incrementAndGet();
            System.out.println("🧮 计算任务 #" + taskId + " (" + taskName + ") 开始执行");
            System.out.println("  📊 复杂度级别: " + complexityLevel);
            System.out.println("  ⏰ 任务开始时间: " + startTime + "ms");
            
            // 执行计算密集型工作
            long result = 0;
            for (int i = 0; i < complexityLevel * 1000000; i++) {
                result += Math.sqrt(i);
            }
            
            // 模拟额外的计算
            for (int i = 0; i < 1000000; i++) {
                result = (result + i) % 1000000;
            }
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            totalExecutionTime.addAndGet(executionTime);
            completedCounter.incrementAndGet();
            
            System.out.println("✅ 计算任务 #" + taskId + " (" + taskName + ") 完成");
            System.out.println("  📈 执行时间: " + executionTime + "ms");
            System.out.println("  🎯 计算结果: " + result);
            System.out.println("  📊 总完成任务数: " + completedCounter.get());
        }
    }
    
    /**
     * I/O密集型任务类 - 模拟I/O密集型工作
     * 展示线程池处理I/O操作的效率
     */
    static class IOTask implements Runnable {
        private final String taskName;
        private final int ioOperations; // I/O操作次数
        private final long delayPerOperation; // 每次I/O操作延迟时间
        
        public IOTask(String taskName, int ioOperations, long delayPerOperation) {
            this.taskName = taskName;
            this.ioOperations = ioOperations;
            this.delayPerOperation = delayPerOperation;
        }
        
        @Override
        public void run() {
            int taskId = taskCounter.incrementAndGet();
            System.out.println("💾 I/O任务 #" + taskId + " (" + taskName + ") 开始执行");
            System.out.println("  📊 I/O操作次数: " + ioOperations);
            System.out.println("  ⏱️ 每次操作延迟: " + delayPerOperation + "ms");
            
            try {
                for (int i = 1; i <= ioOperations; i++) {
                    // 模拟I/O操作（文件读写、网络请求等）
                    System.out.println("  🔄 " + taskName + " 执行I/O操作 " + i + "/" + ioOperations);
                    
                    // 模拟I/O延迟
                    Thread.sleep(delayPerOperation);
                    
                    // 模拟数据处理
                    simulateDataProcessing();
                    
                    // 显示进度
                    int progress = i * 100 / ioOperations;
                    if (i % 10 == 0 || i == ioOperations) {
                        System.out.println("    📈 进度: " + progress + "%");
                    }
                }
                
                long endTime = System.currentTimeMillis();
                totalExecutionTime.addAndGet(endTime - startTimeForTask(taskId));
                completedCounter.incrementAndGet();
                
                System.out.println("✅ I/O任务 #" + taskId + " (" + taskName + ") 完成");
                System.out.println("  📊 总完成任务数: " + completedCounter.get());
            } catch (InterruptedException e) {
                System.err.println("❌ I/O任务 #" + taskId + " (" + taskName + ") 被中断");
                Thread.currentThread().interrupt();
            }
        }
        
        // 简单的数据处理模拟
        private void simulateDataProcessing() {
            // 模拟数据解析和处理
            StringBuilder data = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                data.append("X");
            }
            data.reverse();
        }
    }
    
    // 记录每个任务的开始时间
    private static final ConcurrentHashMap<Integer, Long> taskStartTime = new ConcurrentHashMap<>();
    
    private static long startTimeForTask(int taskId) {
        long time = System.currentTimeMillis();
        taskStartTime.put(taskId, time);
        return time;
    }
    
    /**
     * 定时任务类 - 用于演示调度线程池
     */
    static class ScheduledTask implements Runnable {
        private final String taskName;
        private final int executionCount;
        
        public ScheduledTask(String taskName, int executionCount) {
            this.taskName = taskName;
            this.executionCount = executionCount;
        }
        
        @Override
        public void run() {
            int taskId = taskCounter.incrementAndGet();
            long startTime = System.currentTimeMillis();
            
            System.out.println("⏰ 定时任务 #" + taskId + " (" + taskName + ") 开始执行");
            System.out.println("  📅 任务执行次数: " + executionCount);
            System.out.println("  🕐 执行时间: " + startTime);
            
            // 模拟定时任务的工作
            try {
                Thread.sleep(500); // 模拟工作内容
                
                long endTime = System.currentTimeMillis();
                totalExecutionTime.addAndGet(endTime - startTime);
                completedCounter.incrementAndGet();
                
                System.out.println("✅ 定时任务 #" + taskId + " (" + taskName + ") 完成");
                System.out.println("  ⏱️ 执行耗时: " + (endTime - startTime) + "ms");
            } catch (InterruptedException e) {
                System.err.println("❌ 定时任务 #" + taskId + " 被中断");
            }
        }
    }
    
    /**
     * 字符串重复方法 - 兼容Java 8
     */
    private static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * 字符串填充方法 - 兼容Java 8
     */
    private static String padEnd(String str, int totalLength, String padStr) {
        if (str.length() >= totalLength) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < totalLength) {
            sb.append(padStr);
        }
        return sb.toString();
    }

    /**
     * 主方法 - 演示线程池的各种用法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println(repeat("=", 70));
        System.out.println("🎓 ThreadPoolDemo - 线程池高级应用演示");
        System.out.println(repeat("=", 70));
        
        // 演示1: 固定线程池处理计算密集型任务
        demonstrateFixedThreadPool();
        
        // 演示2: 缓存线程池处理I/O密集型任务
        demonstrateCachedThreadPool();
        
        // 演示3: 单线程池保证任务顺序执行
        demonstrateSingleThreadExecutor();
        
        // 演示4: 调度线程池执行定时任务
        demonstrateScheduledThreadPool();
        
        // 演示5: 线程池监控与统计
        demonstrateThreadPoolMonitoring();
        
        // 演示6: 自定义线程池配置
        demonstrateCustomThreadPool();
        
        // 演示7: 实际应用场景
        demonstrateRealWorldScenarios();
        
        // 总结与最佳实践
        printBestPractices();
    }
    
    /**
     * 演示1: 固定线程池
     * 适合计算密集型任务，线程数量固定
     */
    private static void demonstrateFixedThreadPool() {
        System.out.println("\n" + padEnd("🔸 演示1: 固定线程池（FixedThreadPool）", 60, " "));
        System.out.println(repeat("-", 60));
        System.out.println("💡 特点: 线程数量固定，适合CPU密集型任务");
        System.out.println("🎯 优势: 资源可控，避免过多线程开销");
        System.out.println("⚠️ 注意: 如果任务过多，会排队等待");
        
        // 创建固定大小为4的线程池
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        System.out.println("\n🚀 提交8个计算密集型任务到固定线程池...");
        
        // 提交多个计算任务
        for (int i = 1; i <= 8; i++) {
            ComputationTask task = new ComputationTask("计算任务-" + i, 3);
            executor.submit(task);
        }
        
        // 关闭线程池（不再接受新任务，但执行完队列中的任务）
        executor.shutdown();
        
        try {
            // 等待所有任务完成（最多等待60秒）
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("⏰ 超时，强制关闭线程池");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("❌ 等待任务完成时被中断");
            executor.shutdownNow();
        }
        
        System.out.println("✅ 固定线程池演示完成");
    }
    
    /**
     * 演示2: 缓存线程池
     * 适合I/O密集型任务，线程数量动态变化
     */
    private static void demonstrateCachedThreadPool() {
        System.out.println("\n" + padEnd("🔸 演示2: 缓存线程池（CachedThreadPool）", 60, " "));
        System.out.println(repeat("-", 60));
        System.out.println("💡 特点: 线程数量动态变化，适合I/O密集型任务");
        System.out.println("🎯 优势: 自动回收空闲线程，灵活适应任务量");
        System.out.println("⚠️ 注意: 大量短任务可能创建过多线程");
        
        // 创建缓存线程池（初始线程0，最大线程数Integer.MAX_VALUE）
        ExecutorService executor = Executors.newCachedThreadPool();
        
        System.out.println("\n🌊 提交10个I/O密集型任务到缓存线程池...");
        
        // 提交多个I/O任务
        for (int i = 1; i <= 10; i++) {
            IOTask task = new IOTask("I/O任务-" + i, 5, 200);
            executor.submit(task);
        }
        
        executor.shutdown();
        
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("⏰ 超时，强制关闭线程池");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("❌ 等待任务完成时被中断");
            executor.shutdownNow();
        }
        
        System.out.println("✅ 缓存线程池演示完成");
    }
    
    /**
     * 演示3: 单线程池
     * 保证任务按顺序执行，适用于需要保证执行顺序的场景
     */
    private static void demonstrateSingleThreadExecutor() {
        System.out.println("\n" + padEnd("🔸 演示3: 单线程池（SingleThreadExecutor）", 60, " "));
        System.out.println(repeat("-", 60));
        System.out.println("💡 特点: 只有一个工作线程，按顺序执行任务");
        System.out.println("🎯 优势: 保证任务执行顺序，线程安全");
        System.out.println("⚠️ 注意: 任务会排队执行，耗时任务会影响后续任务");
        
        // 创建单线程池
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        System.out.println("\n🎬 提交5个需要按顺序执行的任务...");
        
        // 提交按顺序执行的任务
        for (int i = 1; i <= 5; i++) {
            final int taskNum = i;
            Runnable task = () -> {
                int taskId = taskCounter.incrementAndGet();
                System.out.println("🎯 顺序任务 #" + taskId + " (任务" + taskNum + ") 开始执行");
                System.out.println("  📅 顺序: " + taskNum);
                
                try {
                    // 模拟任务执行时间
                    Thread.sleep(1000 + taskNum * 200);
                    
                    long endTime = System.currentTimeMillis();
                    totalExecutionTime.addAndGet(endTime - startTimeForTask(taskId));
                    completedCounter.incrementAndGet();
                    
                    System.out.println("✅ 顺序任务 #" + taskId + " (任务" + taskNum + ") 完成");
                } catch (InterruptedException e) {
                    System.err.println("❌ 顺序任务 #" + taskId + " 被中断");
                }
            };
            
            executor.submit(task);
        }
        
        executor.shutdown();
        
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("❌ 等待任务完成时被中断");
            executor.shutdownNow();
        }
        
        System.out.println("✅ 单线程池演示完成");
    }
    
    /**
     * 演示4: 调度线程池
     * 支持定时任务和周期性任务的执行
     */
    private static void demonstrateScheduledThreadPool() {
        System.out.println("\n" + padEnd("🔸 演示4: 调度线程池（ScheduledThreadPool）", 60, " "));
        System.out.println(repeat("-", 60));
        System.out.println("💡 特点: 支持定时任务和周期性任务");
        System.out.println("🎯 优势: 支持延迟执行、周期性执行");
        System.out.println("⚠️ 注意: 适用于定时监控、定时清理等场景");
        
        // 创建调度线程池（大小为2）
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        
        System.out.println("\n⏰ 提交定时任务...");
        
        // 任务1: 延迟2秒执行一次
        ScheduledFuture<?> task1 = scheduler.schedule(
            new ScheduledTask("延迟任务", 1),
            2,
            TimeUnit.SECONDS
        );
        
        // 任务2: 延迟1秒开始，每2秒执行一次，共执行3次
        ScheduledFuture<?> task2 = scheduler.scheduleAtFixedRate(
            new ScheduledTask("周期性任务", 3),
            1,
            2,
            TimeUnit.SECONDS
        );
        
        // 任务3: 延迟500ms开始，上次任务完成后延迟3秒执行，共执行3次
        ScheduledFuture<?> task3 = scheduler.scheduleWithFixedDelay(
            new ScheduledTask("固定延迟任务", 3),
            500,
            3000,
            TimeUnit.MILLISECONDS
        );
        
        System.out.println("📋 定时任务已提交：");
        System.out.println("  1. 延迟任务: 2秒后执行一次");
        System.out.println("  2. 周期性任务: 1秒后开始，每2秒执行一次");
        System.out.println("  3. 固定延迟任务: 0.5秒后开始，任务间隔3秒");
        
        try {
            // 让调度任务运行一段时间
            Thread.sleep(15000); // 15秒后关闭
            
            // 取消剩余的定时任务
            task1.cancel(false);
            task2.cancel(false);
            task3.cancel(false);
            
            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
            
        } catch (InterruptedException e) {
            System.err.println("❌ 调度任务执行被中断");
            scheduler.shutdownNow();
        }
        
        System.out.println("✅ 调度线程池演示完成");
    }
    
    /**
     * 演示5: 线程池监控
     * 展示如何监控线程池的状态和性能
     */
    private static void demonstrateThreadPoolMonitoring() {
        System.out.println("\n" + padEnd("🔸 演示5: 线程池监控与统计", 60, " "));
        System.out.println(repeat("-", 60));
        System.out.println("💡 特点: 监控线程池运行状态、性能指标");
        System.out.println("🎯 优势: 实时了解线程池健康状况");
        
        // 创建自定义配置的线程池用于监控
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                      // 核心线程数
            4,                      // 最大线程数
            60,                     // 空闲线程存活时间
            TimeUnit.SECONDS,       // 时间单位
            new LinkedBlockingQueue<>(5), // 任务队列（容量5）
            new ThreadFactoryBuilder("监控线程池").build(), // 线程工厂
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
        
        System.out.println("\n📊 提交监控任务到自定义线程池...");
        
        // 提交多个任务进行监控
        for (int i = 1; i <= 8; i++) {
            final int taskNum = i;
            Runnable task = () -> {
                try {
                    Thread.sleep(1000 + taskNum * 200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };
            executor.submit(task);
            
            // 每提交一个任务就显示状态
            if (i % 2 == 0) {
                printThreadPoolStatus(executor, "任务" + i + "提交后");
            }
        }
        
        // 最终状态
        printThreadPoolStatus(executor, "所有任务提交完成");
        
        executor.shutdown();
        
        try {
            executor.awaitTermination(20, TimeUnit.SECONDS);
            printThreadPoolStatus(executor, "所有任务执行完成");
        } catch (InterruptedException e) {
            System.err.println("❌ 监控任务执行被中断");
        }
        
        System.out.println("✅ 线程池监控演示完成");
    }
    
    /**
     * 演示6: 自定义线程池配置
     * 展示如何根据具体需求配置线程池参数
     */
    private static void demonstrateCustomThreadPool() {
        System.out.println("\n" + padEnd("🔸 演示6: 自定义线程池配置", 60, " "));
        System.out.println(repeat("-", 60));
        System.out.println("💡 特点: 根据具体业务需求定制线程池");
        System.out.println("🎯 优势: 精确控制资源使用，性能优化");
        
        // 创建适合CPU密集型任务的线程池
        int cpuCores = Runtime.getRuntime().availableProcessors();
        System.out.println("🖥️ 检测到CPU核心数: " + cpuCores);
        
        ThreadPoolExecutor cpuIntensivePool = new ThreadPoolExecutor(
            cpuCores,                    // 核心线程数 = CPU核心数
            cpuCores,                    // 最大线程数 = CPU核心数
            0L,                          // 空闲线程存活时间（计算密集型不需要）
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), // 无界队列
            r -> new Thread(r, "CPU-Worker-" + r.hashCode()),
            new ThreadPoolExecutor.AbortPolicy()
        );
        
        // 创建适合I/O密集型任务的线程池
        ThreadPoolExecutor ioIntensivePool = new ThreadPoolExecutor(
            cpuCores * 2,                // 核心线程数 = CPU核心数 * 2
            cpuCores * 4,                // 最大线程数 = CPU核心数 * 4
            60L,                         // 空闲线程存活时间60秒
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100), // 容量100的队列
            r -> new Thread(r, "IO-Worker-" + r.hashCode()),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        System.out.println("\n🧮 提交CPU密集型任务到CPU优化线程池...");
        // 提交CPU密集型任务
        for (int i = 1; i <= 4; i++) {
            cpuIntensivePool.submit(new ComputationTask("CPU任务-" + i, 5));
        }
        
        System.out.println("\n💾 提交I/O密集型任务到I/O优化线程池...");
        // 提交I/O密集型任务
        for (int i = 1; i <= 6; i++) {
            ioIntensivePool.submit(new IOTask("IO任务-" + i, 3, 300));
        }
        
        // 等待所有任务完成
        cpuIntensivePool.shutdown();
        ioIntensivePool.shutdown();
        
        try {
            cpuIntensivePool.awaitTermination(30, TimeUnit.SECONDS);
            ioIntensivePool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("❌ 自定义线程池任务执行被中断");
        }
        
        System.out.println("✅ 自定义线程池演示完成");
    }
    
    /**
     * 演示7: 实际应用场景
     * 展示线程池在真实项目中的应用
     */
    private static void demonstrateRealWorldScenarios() {
        System.out.println("\n" + padEnd("🔸 演示7: 实际应用场景", 60, " "));
        System.out.println(repeat("-", 60));
        System.out.println("💡 特点: 模拟真实项目中的线程池使用场景");
        
        // 模拟Web服务器线程池
        demonstrateWebServerScenario();
        
        // 模拟文件处理系统
        demonstrateFileProcessingScenario();
        
        // 模拟API调用系统
        demonstrateApiCallScenario();
        
        System.out.println("✅ 实际应用场景演示完成");
    }
    
    /**
     * 模拟Web服务器场景
     */
    private static void demonstrateWebServerScenario() {
        System.out.println("\n🌐 模拟Web服务器场景...");
        
        // Web服务器线程池配置
        ThreadPoolExecutor webServerPool = new ThreadPoolExecutor(
            10,                    // 核心线程数
            50,                    // 最大线程数
            60L,                   // 空闲时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);
                public Thread newThread(Runnable r) {
                    return new Thread(r, "HTTP-Worker-" + counter.incrementAndGet());
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // 模拟HTTP请求处理
        for (int i = 1; i <= 20; i++) {
            final int requestId = i;
            webServerPool.submit(() -> {
                try {
                    System.out.println("🌐 HTTP请求 #" + requestId + " 开始处理");
                    
                    // 模拟请求处理时间
                    Thread.sleep(500 + (int)(Math.random() * 1000));
                    
                    System.out.println("✅ HTTP请求 #" + requestId + " 处理完成");
                } catch (InterruptedException e) {
                    System.err.println("❌ HTTP请求 #" + requestId + " 被中断");
                }
            });
        }
        
        webServerPool.shutdown();
        try {
            webServerPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            webServerPool.shutdownNow();
        }
    }
    
    /**
     * 模拟文件处理系统
     */
    private static void demonstrateFileProcessingScenario() {
        System.out.println("\n📁 模拟文件处理系统...");
        
        ExecutorService fileProcessingPool = Executors.newFixedThreadPool(3);
        
        // 模拟不同类型的文件处理任务
        String[] fileTypes = {"CSV", "JSON", "XML", "TXT", "CSV", "JSON"};
        
        for (int i = 0; i < fileTypes.length; i++) {
            final String fileType = fileTypes[i];
            final int fileId = i + 1;
            
            fileProcessingPool.submit(() -> {
                try {
                    System.out.println("📄 文件处理 #" + fileId + " (" + fileType + ") 开始");
                    
                    // 模拟文件读取和处理
                    Thread.sleep(800 + (int)(Math.random() * 400));
                    
                    System.out.println("✅ 文件处理 #" + fileId + " (" + fileType + ") 完成");
                } catch (InterruptedException e) {
                    System.err.println("❌ 文件处理 #" + fileId + " 被中断");
                }
            });
        }
        
        fileProcessingPool.shutdown();
        try {
            fileProcessingPool.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fileProcessingPool.shutdownNow();
        }
    }
    
    /**
     * 模拟API调用系统
     */
    private static void demonstrateApiCallScenario() {
        System.out.println("\n🔗 模拟API调用系统...");
        
        ExecutorService apiCallPool = Executors.newCachedThreadPool();
        
        // 模拟调用不同的API服务
        String[] apiServices = {"用户服务", "订单服务", "支付服务", "通知服务", "日志服务"};
        
        for (int i = 0; i < apiServices.length; i++) {
            final String service = apiServices[i];
            final int callId = i + 1;
            
            apiCallPool.submit(() -> {
                try {
                    System.out.println("🔗 API调用 #" + callId + " -> " + service + " 开始");
                    
                    // 模拟API响应时间
                    Thread.sleep(300 + (int)(Math.random() * 700));
                    
                    // 模拟API响应
                    boolean success = Math.random() > 0.1; // 90%成功率
                    if (success) {
                        System.out.println("✅ API调用 #" + callId + " -> " + service + " 成功");
                    } else {
                        System.err.println("❌ API调用 #" + callId + " -> " + service + " 失败");
                    }
                } catch (InterruptedException e) {
                    System.err.println("❌ API调用 #" + callId + " 被中断");
                }
            });
        }
        
        apiCallPool.shutdown();
        try {
            apiCallPool.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            apiCallPool.shutdownNow();
        }
    }
    
    /**
     * 打印线程池当前状态
     */
    private static void printThreadPoolStatus(ThreadPoolExecutor executor, String context) {
        System.out.println("\n📊 " + context + " 线程池状态:");
        System.out.println("  🏊 活跃线程数: " + executor.getActiveCount());
        System.out.println("  ⏳ 排队任务数: " + executor.getQueue().size());
        System.out.println("  ✅ 已完成任务数: " + executor.getCompletedTaskCount());
        System.out.println("  📝 任务总数: " + executor.getTaskCount());
    }
    
    /**
     * 线程工厂构建器类
     */
    static class ThreadFactoryBuilder {
        private String namePrefix;
        private boolean daemon = false;
        private int priority = Thread.NORM_PRIORITY;
        
        public ThreadFactoryBuilder(String namePrefix) {
            this.namePrefix = namePrefix;
        }
        
        public ThreadFactoryBuilder daemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }
        
        public ThreadFactoryBuilder priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public ThreadFactory build() {
            return new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);
                
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, namePrefix + "-" + counter.incrementAndGet());
                    thread.setDaemon(daemon);
                    thread.setPriority(priority);
                    return thread;
                }
            };
        }
    }
    
    /**
     * 打印线程池最佳实践
     */
    private static void printBestPractices() {
        System.out.println("\n" + padEnd("🎯 线程池最佳实践指南", 60, " "));
        System.out.println(repeat("-", 60));
        
        System.out.println("🏗️ 线程池配置原则:");
        System.out.println("  • CPU密集型: 核心线程数 = CPU核心数");
        System.out.println("  • I/O密集型: 核心线程数 = CPU核心数 × 2");
        System.out.println("  • 混合型: 根据实际测试调整");
        
        System.out.println("\n📊 队列选择策略:");
        System.out.println("  • LinkedBlockingQueue: 有界队列，防止内存溢出");
        System.out.println("  • ArrayBlockingQueue: 有界，性能更好");
        System.out.println("  • SynchronousQueue: 直接提交，需要更多线程");
        
        System.out.println("\n⚠️ 拒绝策略选择:");
        System.out.println("  • AbortPolicy: 直接抛出异常（默认）");
        System.out.println("  • CallerRunsPolicy: 由调用线程执行");
        System.out.println("  • DiscardPolicy: 丢弃任务");
        System.out.println("  • DiscardOldestPolicy: 丢弃队列最前面的任务");
        
        System.out.println("\n🔧 监控要点:");
        System.out.println("  • 监控队列大小，防止任务堆积");
        System.out.println("  • 监控线程活跃数，优化线程配置");
        System.out.println("  • 监控任务执行时间，发现性能瓶颈");
        System.out.println("  • 监控拒绝任务数，调整系统容量");
        
        System.out.println("\n💡 性能优化建议:");
        System.out.println("  • 根据任务类型选择合适的线程池");
        System.out.println("  • 设置合理的核心线程数和最大线程数");
        System.out.println("  • 选择合适的任务队列类型和大小");
        System.out.println("  • 实现自定义ThreadFactory为线程命名");
        System.out.println("  • 定期监控和调优线程池配置");
        
        // 显示总体统计
        System.out.println("\n📈 本次演示总体统计:");
        System.out.println("  🎯 总任务数: " + taskCounter.get());
        System.out.println("  ✅ 完成任务数: " + completedCounter.get());
        System.out.println("  ⏱️ 总执行时间: " + totalExecutionTime.get() + "ms");
        
        System.out.println("\n🎉 ThreadPoolDemo演示完成！");
    }
}