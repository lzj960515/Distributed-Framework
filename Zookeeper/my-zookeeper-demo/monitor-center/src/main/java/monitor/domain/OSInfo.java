package monitor.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 操作系统信息
 *
 * @author Zijian Liao
 * @since 1.0.0
 */


@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class OSInfo {
    public String ip;
    public Double cpu;
    public long usedMemorySize;
    public long usableMemorySize;
    public String pid;
    public long lastUpdateTime;

}
