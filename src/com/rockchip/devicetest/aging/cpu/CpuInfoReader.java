/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月15日 下午5:01:01  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月15日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.aging.cpu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;
import android.util.Log;

import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.StringUtils;

public class CpuInfoReader {

	public static final String SOC = "/sys/devices/system/cpu/soc";
	public static final String SCALING_CUR_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
	public static final String CPUINFO_MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
	public static final String CPUINFO_MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
	public static final String SCALING_AVAILABLE_FREQUENCIES = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
	public static final String SCALING_GPU_FREQ = "/sys/bus/platform/drivers/mali/ffa30000.gpu/clock";
//	public static final String CPUINFO_CPU_TEMP = "/sys/devices/ff280000.tsadc/temp0_input";
	public static final String CPUINFO_CPU_TEMP ="/sys/class/thermal/thermal_zone0/temp";


	public static float getProcessCpuRate() {
		float totalCpuTime1 = getCpuTime()[0];
		float processCpuTime1 = getAppCpuTime();
		try {
			Thread.sleep(360);
		} catch (Exception e) {
		}

		float totalCpuTime2 = getCpuTime()[0];
		float processCpuTime2 = getAppCpuTime();

		float cpuRate = 100 * (processCpuTime2 - processCpuTime1) / (totalCpuTime2 - totalCpuTime1);

		return cpuRate;
	}

	// 获取系统总CPU使用时间和空闲时间
	public static long[] getCpuTime() {
		long[] cpuInfo = new long[10];
		String[] cpuInfos = null;

		String[] cpu0Infos = null;
		String[] cpu1Infos = null;
		String[] cpu2Infos = null;
		String[] cpu3Infos = null;

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1000);
			String load = reader.readLine();
			String cpu0 = reader.readLine();
			String cpu1 = reader.readLine();
			String cpu2 = reader.readLine();
			String cpu3 = reader.readLine();

			reader.close();
			cpuInfos = load.split(" ");
			cpu0Infos = cpu0.split(" ");
			cpu1Infos = cpu1.split(" ");
			cpu2Infos = cpu2.split(" ");
			cpu3Infos = cpu3.split(" ");
		} catch (IOException ex) {
			ex.printStackTrace();
			return cpuInfo;
		}

		long totalCpu = Long.parseLong(cpuInfos[2]) + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
				+ Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5]) + Long.parseLong(cpuInfos[7])
				+ Long.parseLong(cpuInfos[8]);

		long totalCpu0 = Long.parseLong(cpu0Infos[2]) + Long.parseLong(cpu0Infos[3]) + Long.parseLong(cpu0Infos[4])
				+ Long.parseLong(cpu0Infos[6]) + Long.parseLong(cpu0Infos[5]) + Long.parseLong(cpu0Infos[7])
				+ Long.parseLong(cpu0Infos[8]);

		long totalCpu1 = Long.parseLong(cpu1Infos[2]) + Long.parseLong(cpu1Infos[3]) + Long.parseLong(cpu1Infos[4])
				+ Long.parseLong(cpu1Infos[6]) + Long.parseLong(cpu1Infos[5]) + Long.parseLong(cpu1Infos[7])
				+ Long.parseLong(cpu1Infos[8]);

		long totalCpu2 = Long.parseLong(cpu2Infos[2]) + Long.parseLong(cpu2Infos[3]) + Long.parseLong(cpu2Infos[4])
				+ Long.parseLong(cpu2Infos[6]) + Long.parseLong(cpu2Infos[5]) + Long.parseLong(cpu2Infos[7])
				+ Long.parseLong(cpu2Infos[8]);

//		long totalCpu3 = Long.parseLong(cpu3Infos[2]) + Long.parseLong(cpu3Infos[3]) + Long.parseLong(cpu3Infos[4])
//				+ Long.parseLong(cpu3Infos[6]) + Long.parseLong(cpu3Infos[5]) + Long.parseLong(cpu3Infos[7])
//				+ Long.parseLong(cpu3Infos[8]);

		cpuInfo[0] = totalCpu;
		cpuInfo[1] = Long.parseLong(cpuInfos[5]);

		cpuInfo[2] = totalCpu0;
		cpuInfo[3] = Long.parseLong(cpu0Infos[4]);

		cpuInfo[4] = totalCpu1;
		cpuInfo[5] = Long.parseLong(cpu1Infos[4]);

		cpuInfo[6] = totalCpu2;
		cpuInfo[7] = Long.parseLong(cpu2Infos[4]);

//		cpuInfo[8] = totalCpu3;
//		cpuInfo[9] = Long.parseLong(cpu3Infos[4]);

		return cpuInfo;
	}

	public static long getAppCpuTime() {
		String[] cpuInfos = null;
		try {
			int pid = android.os.Process.myPid();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid
					+ "/stat")), 1000);
			String load = reader.readLine();
			reader.close();
			cpuInfos = load.split(" ");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		long appCpuTime = Long.parseLong(cpuInfos[13]) + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
				+ Long.parseLong(cpuInfos[16]);
		return appCpuTime;
	}

	/**
	 * 获取CPU核数
	 * 
	 * @return
	 */
	public static int getCpuCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			e.printStackTrace();
			// Default to return 1 core
			return -1;
		}
	}

	/**
	 * 获取CPU型号
	 * 
	 * @return
	 */
	public static String getCpuModel() {
		return FileUtils.readFromFile(new File(SOC));
	}

	/**
	 * 获取CPU最低频率
	 * 
	 * @return
	 */
	public static int getCpuMinFreq() {
		String minfreq = FileUtils.readFromFile(new File(CPUINFO_MIN_FREQ));
		return StringUtils.parseInt(minfreq, -1);
	}

	/**
	 * 获取CPU最高频率
	 * 
	 * @return
	 */
	public static int getCpuMaxFreq() {
		String maxfreq = FileUtils.readFromFile(new File(CPUINFO_MAX_FREQ));
		return StringUtils.parseInt(maxfreq, -1);
	}

	/**
	 * 获取CPU当前频率
	 * 
	 * @return
	 */
	public static int getCpuCurrentFreq() {
		String currfreq = FileUtils.readFromFile(new File(SCALING_CUR_FREQ));// cpuinfo_cur_freq
		return StringUtils.parseInt(currfreq, -1);
	}

	/*
	 * public static List<Integer> getAvailableFrequencies() { String freqs =
	 * FileUtils.readFromFile(new File(SCALING_AVAILABLE_FREQUENCIES)); }
	 */

	public static int getGpuCurrentFreq() {
		String currfreq = FileUtils.readFromFile(new File(SCALING_GPU_FREQ));// cpuinfo_cur_freq
		String frep = "";
		if (currfreq != null) {
			frep = currfreq.substring(19, 22);
		}
		return StringUtils.parseInt(frep, -1);
	}

	public static int getCpuTemp() {
		String currfreq = FileUtils.readFromFile(new File(CPUINFO_CPU_TEMP));// cpu
																				// tempreture
		if (currfreq != null) {
			return StringUtils.parseInt(currfreq, -1);
		} else {
			return 0;
		}
	}
}
