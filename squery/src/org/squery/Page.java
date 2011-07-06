package org.squery;



/**
 * 封装分页和排序参数及分页查询结果.
 */
public class Page {

	public static int DEFAULT_PAGESIZE = 10;

	public static int DEFAULT_PAGE = 1;

	private int pageNo = DEFAULT_PAGE;
	private int pageSize = DEFAULT_PAGESIZE;
	private int totalCount = -1;

	public Page() {
	}

	public Page(int pageNo) {
		this.pageNo = pageNo;
	}

	public Page(int pageNo, int pageSize) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}

	public Page(int pageNo, int pageSize, int totalCount) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.totalCount = totalCount;
	}

	/**
	 * 第一条记录在结果集中的位置,序号从0开始.
	 */
	public int getStart() {
		return getOffset();
	}

	public int getOffset() {
        if(pageNo < 0 || pageSize < 0)
            return -1;
        else
            return (pageNo - 1) * pageSize;
    }

	/**
	 * 总页数.
	 */
	public int getTotalPageCount() {
		int count = totalCount / pageSize;
		if (totalCount % pageSize > 0) {
			count++;
		}
		return count;
	}

	/**
	 * 是否还有下一页.
	 */
	public boolean hasNextPage() {
		return (pageNo + 1 <= getTotalPageCount());
	}

	/**
	 * 返回下页的页号,序号从1开始.
	 */
	public int getNextPage() {
		if (hasNextPage())
			return pageNo + 1;
		else
			return pageNo;
	}

	/**
	 * 是否还有上一页.
	 */
	public boolean hasPrePage() {
		return (pageNo - 1 >= 1);
	}

	/**
	 * 返回上页的页号,序号从1开始.
	 */
	public int getPrePage() {
		if (hasPrePage())
			return pageNo - 1;
		else
			return pageNo;
	}

	/**
	 * 每页的记录数量.
	 */
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * 当前页的页号,序号从1开始.
	 */
	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int page) {
		this.pageNo = page;
	}

	/**
	 * 总记录数量.
	 */
	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
}

