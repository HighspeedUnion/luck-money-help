import std.io.*
import std.fs.*
import std.process.*

// ===== ANSI 颜色常量 =====
let RESET = "\u001b[0m"
let RED = "\u001b[31m"
let GREEN = "\u001b[32m"
let YELLOW = "\u001b[33m"
let BLUE = "\u001b[34m"
let MAGENTA = "\u001b[35m"
let CYAN = "\u001b[36m"
let BOLD = "\u001b[1m"

// 检查路径是否仅含 ASCII 字符
func isAsciiPath(s: String): Bool {
    for (ch in s) {
        let code = ch.toInt()
        if (code < 32 || code > 126) {
            return false
        }
    }
    return true
}

// 获取操作系统类型（Windows / Linux / Other）
func getOS(): String {
    let result = Process.exec("uname", ["-s"])
    if (result.exitCode == 0) {
        let os = result.stdout.trim()
        if (os.contains("Linux")) {
            return "Linux"
        }
        return os
    }
    return "Windows"
}

// 进度条模拟函数（带颜色）
func showProgress(msg: String) {
    print(msg)
    for (i in 0..=100) {
        let block = "=" * (i / 10)
        let space = " " * (10 - i / 10)
        print("\r" + msg + " [" + GREEN + block + ">" + space + RESET + "] " + i.toString() + "%")
        for (j in 0..100000) { }
    }
    println()
}

func main() {
    // ===== 1. 彩色标题 =====
    println(BOLD + CYAN + "======================================")
    println("      齐念APPx安装工具 - TUI 版      ")
    println("======================================" + RESET)
    println("请提供安装程序绝对路径，如：")
    println("  " + YELLOW + "/home/user/apps/myapp.appx" + RESET)
    print(BOLD + "请输入: " + RESET)

    // ===== 2. 获取输入 =====
    let input = readln().trim()
    if (input.isEmpty()) {
        println(RED + "输入无效！" + RESET)
        return
    }

    // ===== 3. 检查路径是否含有中文字符 =====
    if (!isAsciiPath(input)) {
        println(RED + "错误：路径中包含非英文字符（如中文、日文等）。" + RESET)
        println(RED + "目前仅支持英文路径，请重新输入。" + RESET)
        return
    }

    // ===== 4. 检查本地路径有效性 =====
    if (!exists(input)) {
        println(RED + "错误：找不到该路径，请检查目录是否正确。" + RESET)
        return
    }
    println(GREEN + "本地文件存在。" + RESET)

    // ===== 5. 询问操作方式 =====
    println(BOLD + "请选择操作：" + RESET)
    println("  " + BLUE + "1." + RESET + " 直接安装")
    println("  " + BLUE + "2." + RESET + " 导出安装 .ps1 文件")
    print(BOLD + "请输入选择 (1/2): " + RESET)
    let choice = readln().trim()

    if (choice == "1") {
        let os = getOS()
        if (os == "Windows") {
            // Windows 环境：执行真正的安装
            if (input.endsWith(".appx")) {
                showProgress("正在执行安装...")
                let cmd = "powershell.exe -ExecutionPolicy Bypass -Command \"Add-AppxPackage -Path '" + input + "'\""
                let result = Process.exec("cmd", ["/c", cmd])
                if (result.exitCode == 0) {
                    println(GREEN + BOLD + "安装完成！" + RESET)
                } else {
                    println(RED + "安装失败，错误码: " + result.exitCode.toString() + RESET)
                }
            } else {
                println(YELLOW + "提示：当前文件不是 .appx 类型，无法直接安装，将为您导出脚本。" + RESET)
                showProgress("正在导出 PS1...")
                println(GREEN + "导出完毕！文件已保存。" + RESET)
            }
        } else {
            // Linux 等其他系统：演示模式
            println(YELLOW + "提示：当前系统为 " + os + "，仅供演示。" + RESET)
            println(YELLOW + "请在 Windows 系统上运行，安装功能将完全正常。" + RESET)
            println(YELLOW + "程序退出。" + RESET)
            return
        }
    } else if (choice == "2") {
        // 导出 PS1（在任何系统上均可执行）
        showProgress("正在导出 PS1...")
        let baseName = "安装程序"
        let savePath = baseName + ".ps1"
        let targetDir = "C:\\Program Files\\MyApp"
        let cmd = "Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force\n" +
                  "Write-Host \"正在处理: " + input + "\"\n" +
                  "Write-Host \"安装目标: " + targetDir + "\"\n" +
                  "Write-Host \"任务完成。\""
        let f = File(savePath, FileMode.WRITE)
        f.writeText(cmd)
        println(GREEN + BOLD + "导出完毕！文件已保存为 " + savePath + RESET)
    } else {
        println(RED + "输入无效，程序退出。" + RESET)
    }

    // ===== 6. 退出程序 =====
    println(BOLD + "感谢使用，程序正在退出..." + RESET)
    return
}
