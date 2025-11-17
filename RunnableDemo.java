/**
 * RunnableDemo - 演示通过实现Runnable接口创建线程
 * 
 * 本类展示了Java多线程编程的第二种创建方式：实现Runnable接口
 * 包含完整的理论说明和实际应用场景，以及与Thread继承方式的对比
 * 
 * @author Java Learning Tutorial
 * @version 1.0
 * @date 2024
 */

public class RunnableDemo {
    
    /**
     * 任务执行器线程类 - 实现Runnable接口
     * 这是创建线程的更灵活的方式
     */
    static class TaskExecutor implements Runnable {
        private String taskName;
        private int taskId;
        private int duration; // 任务持续时间（毫秒）
        
        /**
         * 任务执行器构造函数
         * @param taskName 任务名称
         * @param taskId 任务ID
         * @param duration 任务持续时间（毫秒）
         */
        public TaskExecutor(String taskName, int taskId, int duration) {
            this.taskName = taskName;
            this.taskId = taskId;
            this.duration = duration;
        }
        
        /**
         * 重写run()方法 - 线程执行的具体逻辑
         * 注意：Runnable接口只有这一个抽象方法
         */
        @Override
        public void run() {
            System.out.println("🏃 任务 " + taskName + " (ID: " + taskId + ") 开始执行");
            System.out.println("⏰ 预计执行时间: " + duration + " 毫秒");
            
            // 模拟任务执行过程
            int totalSteps = 10;
            for (int step = 1; step <= totalSteps; step++) {
                // 模拟工作内容
                simulateWork();
                
                // 显示进度
                int progress = step * 100 / totalSteps;
                System.out.println("📊 " + taskName + " 进度: " + progress + "% (步骤 " + 
                                 step + "/" + totalSteps + ")");
                
                // 随机延迟模拟实际工作
                try {
                    Thread.sleep(duration / totalSteps);
                } catch (InterruptedException e) {
                    System.err.println("❌ 任务 " + taskName + " 被中断");
                    Thread.currentThread().interrupt(); // 重新设置中断状态
                    return;
                }
            }
            
            System.out.println("✅ 任务 " + taskName + " (ID: " + taskId + ") 执行完毕");
            System.out.println("🎯 " + taskName + " 任务完成时间: " + 
                             System.currentTimeMillis() + "ms");
        }
        
        /**
         * 模拟工作任务
         */
        private void simulateWork() {
            // 模拟CPU密集型计算
            long sum = 0;
            for (int i = 0; i < 100000; i++) {
                sum += i;
            }
        }
        
        /**
         * 获取任务信息
         * @return 任务信息字符串
         */
        public String getTaskInfo() {
            return String.format("任务: %s (ID: %d), 预计时间: %dms", 
                               taskName, taskId, duration);
        }
    }
    
    /**
     * 数据库操作线程类 - 展示实际应用场景
     * 模拟数据库并发操作
     */
    static class DatabaseOperator implements Runnable {
        private String operation;
        private int recordsCount;
        private String threadName;
        
        public DatabaseOperator(String operation, int recordsCount, String threadName) {
            this.operation = operation;
            this.recordsCount = recordsCount;
            this.threadName = threadName;
        }
        
        @Override
        public void run() {
            System.out.println("🗄️  " + threadName + " 开始执行数据库操作");
            System.out.println("📝 操作类型: " + operation);
            System.out.println("📊 处理记录数: " + recordsCount);
            
            int batchSize = 100; // 每批处理100条记录
            int processedCount = 0;
            
            while (processedCount < recordsCount) {
                // 模拟数据库操作
                int currentBatch = Math.min(batchSize, recordsCount - processedCount);
                
                System.out.println("🔄 " + threadName + " 正在处理第 " + 
                                 (processedCount + 1) + "-" + 
                                 (processedCount + currentBatch) + " 条记录");
                
                // 模拟数据库处理时间
                try {
                    Thread.sleep(100 + (int)(Math.random() * 200));
                } catch (InterruptedException e) {
                    System.err.println("❌ " + threadName + " 数据库操作被中断");
                    break;
                }
                
                processedCount += currentBatch;
                int progress = processedCount * 100 / recordsCount;
                System.out.println("📈 " + threadName + " 完成度: " + progress + "%");
            }
            
            System.out.println("🎉 " + threadName + " 数据库操作完成！");
        }
    }
    
    /**
     * 网络请求线程类 - 展示异步网络操作
     * 模拟并发网络请求处理
     */
    static class NetworkRequestHandler implements Runnable {
        private String requestUrl;
        private String requestMethod;
        private int requestId;
        
        public NetworkRequestHandler(String requestUrl, String requestMethod, int requestId) {
            this.requestUrl = requestUrl;
            this.requestMethod = requestMethod;
            this.requestId = requestId;
        }
        
        @Override
        public void run() {
            System.out.println("🌐 网络请求 #" + requestId + " 开始处理");
            System.out.println("🔗 URL: " + requestUrl);
            System.out.println("📡 方法: " + requestMethod);
            
            // 模拟网络请求过程
            String[] steps = {"连接服务器", "发送请求", "等待响应", "接收数据", "处理响应"};
            
            for (int i = 0; i < steps.length; i++) {
                System.out.println("📤 请求 #" + requestId + " - " + steps[i]);
                
                // 模拟网络延迟
                try {
                    Thread.sleep(200 + (int)(Math.random() * 300));
                } catch (InterruptedException e) {
                    System.err.println("❌ 请求 #" + requestId + " 被中断");
                    break;
                }
                
                // 模拟成功响应
                System.out.println("✅ 请求 #" + requestId + " - " + steps[i] + " 完成");
            }
            
            System.out.println("🎯 请求 #" + requestId + " 处理完毕");
        }
    }
    
    /**
     * Java 8兼容方法 - 字符串重复
     */
    private static String repeat(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * Java 8兼容方法 - 字符串右侧填充
     */
    private static String padEnd(String str, int length, String padStr) {
        if (str.length() >= length) return str;
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(padStr);
        }
        return sb.toString();
    }
    
    /**
     * 主方法 - 演示Runnable接口的各种用法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println(repeat("=", 60));
        System.out.println("🎓 RunnableDemo - 实现Runnable接口创建线程演示");
        System.out.println(repeat("=", 60));
        
        // 展示1: 基本Runnable实现
        demonstrateBasicRunnable();
        
        // 展示2: 匿名内部类实现
        demonstrateAnonymousClass();
        
        // 展示3: Lambda表达式实现（Java 8+）
        demonstrateLambdaExpression();
        
        // 展示4: 实际应用场景 - 数据库操作
        demonstrateDatabaseOperations();
        
        // 展示5: 实际应用场景 - 网络请求
        demonstrateNetworkRequests();
        
        // 展示6: 线程控制与生命周期
        demonstrateThreadControl();
        
        // 展示7: 对比分析
        printComparisonAnalysis();
    }
    
    /**
     * 演示1: 基本Runnable接口实现
     * 展示最基础的Runnable使用方式
     */
    private static void demonstrateBasicRunnable() {
        System.out.println("\n" + padEnd("🔸 演示1: 基本Runnable接口实现", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 创建Runnable实现类的实例
        TaskExecutor task1 = new TaskExecutor("数据处理", 1, 2000);
        TaskExecutor task2 = new TaskExecutor("文件上传", 2, 1500);
        
        // 创建Thread对象，将Runnable实例包装成线程
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);
        
        System.out.println("📋 任务信息：");
        System.out.println("  " + task1.getTaskInfo());
        System.out.println("  " + task2.getTaskInfo());
        
        System.out.println("\n🚀 启动任务线程...");
        thread1.start();
        thread2.start();
        
        // 等待所有任务完成
        try {
            thread1.join();
            thread2.join();
            System.out.println("\n✅ 所有基础任务执行完毕");
        } catch (InterruptedException e) {
            System.err.println("❌ 主线程被中断");
        }
    }
    
    /**
     * 演示2: 匿名内部类实现
     * 展示使用匿名内部类创建Runnable的方式
     */
    private static void demonstrateAnonymousClass() {
        System.out.println("\n" + padEnd("🔸 演示2: 匿名内部类实现", 50, " "));
        System.out.println(repeat("-", 50));
        
        System.out.println("📝 使用匿名内部类创建多个任务...");
        
        // 创建多个匿名Runnable任务
        Thread[] threads = new Thread[3];
        
        threads[0] = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("🎨 匿名任务1: 图片处理开始");
                for (int i = 1; i <= 5; i++) {
                    System.out.println("🎨 正在处理图片 " + i + "/5");
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                System.out.println("✅ 匿名任务1: 图片处理完成");
            }
        }, "图片处理线程");
        
        threads[1] = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("🔧 匿名任务2: 数据验证开始");
                for (int i = 1; i <= 5; i++) {
                    System.out.println("🔧 正在验证数据块 " + i + "/5");
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                System.out.println("✅ 匿名任务2: 数据验证完成");
            }
        }, "数据验证线程");
        
        threads[2] = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("📧 匿名任务3: 邮件发送开始");
                for (int i = 1; i <= 5; i++) {
                    System.out.println("📧 正在发送邮件 " + i + "/5");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                System.out.println("✅ 匿名任务3: 邮件发送完成");
            }
        }, "邮件发送线程");
        
        // 显示所有线程信息
        System.out.println("\n📋 匿名线程信息：");
        for (Thread thread : threads) {
            System.out.println("  线程名: " + thread.getName() + ", 优先级: " + thread.getPriority());
        }
        
        // 启动所有线程
        System.out.println("\n🚀 启动匿名内部类线程...");
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待完成
        try {
            for (Thread thread : threads) {
                thread.join();
            }
            System.out.println("\n✅ 所有匿名任务执行完毕");
        } catch (InterruptedException e) {
            System.err.println("❌ 匿名任务被中断");
        }
    }
    
    /**
     * 演示3: Lambda表达式实现（Java 8+）
     * 展示现代化的线程创建方式
     */
    private static void demonstrateLambdaExpression() {
        System.out.println("\n" + padEnd("🔸 演示3: Lambda表达式实现", 50, " "));
        System.out.println(repeat("-", 50));
        
        System.out.println("🎯 使用Lambda表达式创建简洁的任务...");
        
        // 使用Lambda表达式创建Runnable任务
        Runnable task1 = () -> {
            System.out.println("⚡ Lambda任务1: 实时数据处理");
            for (int i = 1; i <= 5; i++) {
                System.out.println("⚡ 实时数据处理 - 批次 " + i);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("✅ Lambda任务1: 实时处理完成");
        };
        
        Runnable task2 = () -> {
            System.out.println("📊 Lambda任务2: 统计分析");
            for (int i = 1; i <= 5; i++) {
                System.out.println("📊 统计分析 - 阶段 " + i);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("✅ Lambda任务2: 统计分析完成");
        };
        
        // 使用更简洁的Lambda方式
        Runnable task3 = () -> {
            System.out.println("🔄 Lambda任务3: 缓存更新");
            for (int i = 1; i <= 5; i++) {
                System.out.println("🔄 缓存更新 - 循环 " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("✅ Lambda任务3: 缓存更新完成");
        };
        
        // 创建线程并启动
        Thread lambdaThread1 = new Thread(task1, "Lambda-数据处理");
        Thread lambdaThread2 = new Thread(task2, "Lambda-统计分析");
        Thread lambdaThread3 = new Thread(task3, "Lambda-缓存更新");
        
        System.out.println("\n📋 Lambda线程信息：");
        System.out.println("  " + lambdaThread1.getName());
        System.out.println("  " + lambdaThread2.getName());
        System.out.println("  " + lambdaThread3.getName());
        
        System.out.println("\n🚀 启动Lambda线程...");
        lambdaThread1.start();
        lambdaThread2.start();
        lambdaThread3.start();
        
        // 等待完成
        try {
            lambdaThread1.join();
            lambdaThread2.join();
            lambdaThread3.join();
            System.out.println("\n✅ 所有Lambda任务执行完毕");
        } catch (InterruptedException e) {
            System.err.println("❌ Lambda任务被中断");
        }
    }
    
    /**
     * 演示4: 实际应用场景 - 数据库操作
     * 展示Runnable在数据库并发操作中的应用
     */
    private static void demonstrateDatabaseOperations() {
        System.out.println("\n" + padEnd("🔸 演示4: 数据库操作应用场景", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 创建多个数据库操作任务
        DatabaseOperator insertTask = new DatabaseOperator("INSERT", 500, "数据插入线程");
        DatabaseOperator updateTask = new DatabaseOperator("UPDATE", 300, "数据更新线程");
        DatabaseOperator deleteTask = new DatabaseOperator("DELETE", 200, "数据删除线程");
        
        Thread insertThread = new Thread(insertTask);
        Thread updateThread = new Thread(updateTask);
        Thread deleteThread = new Thread(deleteTask);
        
        System.out.println("🗄️ 启动数据库并发操作...");
        System.out.println("  插入操作: 500条记录");
        System.out.println("  更新操作: 300条记录");
        System.out.println("  删除操作: 200条记录");
        
        // 启动所有数据库线程
        insertThread.start();
        updateThread.start();
        deleteThread.start();
        
        // 等待完成
        try {
            insertThread.join();
            updateThread.join();
            deleteThread.join();
            System.out.println("\n✅ 所有数据库操作完成");
        } catch (InterruptedException e) {
            System.err.println("❌ 数据库操作被中断");
        }
    }
    
    /**
     * 演示5: 实际应用场景 - 网络请求
     * 展示Runnable在网络并发请求中的应用
     */
    private static void demonstrateNetworkRequests() {
        System.out.println("\n" + padEnd("🔸 演示5: 网络请求应用场景", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 创建多个网络请求任务
        NetworkRequestHandler[] handlers = {
            new NetworkRequestHandler("https://api.example.com/users", "GET", 1),
            new NetworkRequestHandler("https://api.example.com/posts", "GET", 2),
            new NetworkRequestHandler("https://api.example.com/comments", "POST", 3),
            new NetworkRequestHandler("https://api.example.com/profile", "PUT", 4)
        };
        
        Thread[] requestThreads = new Thread[handlers.length];
        
        System.out.println("🌐 启动并发网络请求...");
        for (int i = 0; i < handlers.length; i++) {
            requestThreads[i] = new Thread(handlers[i], "网络请求-" + (i + 1));
            requestThreads[i].start();
        }
        
        // 等待所有请求完成
        try {
            for (Thread thread : requestThreads) {
                thread.join();
            }
            System.out.println("\n✅ 所有网络请求处理完毕");
        } catch (InterruptedException e) {
            System.err.println("❌ 网络请求被中断");
        }
    }
    
    /**
     * 演示6: 线程控制与生命周期
     * 展示对Runnable线程的控制方法
     */
    private static void demonstrateThreadControl() {
        System.out.println("\n" + padEnd("🔸 演示6: 线程控制与生命周期", 50, " "));
        System.out.println(repeat("-", 50));
        
        // 创建一个可控制的任务
        Runnable controlledTask = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i <= 20; i++) {
                        // 检查线程是否被中断
                        if (Thread.currentThread().isInterrupted()) {
                            System.out.println("🛑 任务检测到中断请求，准备停止...");
                            break;
                        }
                        
                        System.out.println("📊 任务进度: " + (i * 5) + "%");
                        
                        // 模拟工作
                        Thread.sleep(200);
                    }
                    System.out.println("✅ 任务正常完成");
                } catch (InterruptedException e) {
                    System.err.println("❌ 任务被强制中断");
                }
            }
        };
        
        Thread controlledThread = new Thread(controlledTask, "可控任务线程");
        
        System.out.println("🎮 启动可控任务...");
        controlledThread.start();
        
        // 让任务运行一段时间后中断
        try {
            Thread.sleep(1000); // 运行1秒
            System.out.println("⏹️ 请求中断任务...");
            controlledThread.interrupt(); // 中断线程
            
            controlledThread.join(); // 等待线程结束
            System.out.println("✅ 任务控制演示完成");
        } catch (InterruptedException e) {
            System.err.println("❌ 控制演示被中断");
        }
    }
    
    /**
     * 演示7: 对比分析
     * 对比实现Runnable与继承Thread的优缺点
     */
    private static void printComparisonAnalysis() {
        System.out.println("\n" + padEnd("📚 Runnable vs Thread 继承方式对比分析", 50, " "));
        System.out.println(repeat("-", 50));
        
        System.out.println("🎯 实现Runnable接口方式的优势:");
        System.out.println("  ✅ 避免Java单继承限制");
        System.out.println("  ✅ 更好的代码复用性");
        System.out.println("  ✅ 任务与线程分离，设计更清晰");
        System.out.println("  ✅ 适合线程池管理");
        System.out.println("  ✅ 支持Lambda表达式（Java 8+）");
        System.out.println("  ✅ 更灵活的线程创建和管理");
        
        System.out.println("\n❌ 实现Runnable接口方式的劣势:");
        System.out.println("  • 需要额外的Thread对象包装");
        System.out.println("  • 不能直接使用Thread类的方法");
        System.out.println("  • 代码稍微复杂一些");
        
        System.out.println("\n💡 最佳实践建议:");
        System.out.println("  • 优先使用实现Runnable接口");
        System.out.println("  • 复杂线程逻辑使用Runnable");
        System.out.println("  • 简单任务可使用Lambda表达式");
        System.out.println("  • 企业级应用推荐Runnable + 线程池");
        
        // 实际演示对比
        System.out.println("\n🔄 实际运行对比演示:");
        
        // Thread继承方式
        Thread extendsThread = new Thread() {
            @Override
            public void run() {
                System.out.println("  📝 Thread继承方式: 任务执行中...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
                System.out.println("  ✅ Thread继承方式: 任务完成");
            }
        };
        
        // Runnable实现方式
        Thread runnableThread = new Thread(() -> {
            System.out.println("  📝 Runnable实现方式: 任务执行中...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            System.out.println("  ✅ Runnable实现方式: 任务完成");
        });
        
        extendsThread.start();
        runnableThread.start();
        
        try {
            extendsThread.join();
            runnableThread.join();
        } catch (InterruptedException e) {}
        
        System.out.println("\n🎉 Runnable演示完成！");
    }
}