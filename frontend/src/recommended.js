// 推荐内容源（新闻源 + 书源），供「设置」页一键导入。
// 说明：书源为第三方免费站点，域名/结构可能变动或限流，失效后可自行编辑或重新导入。

export const RECOMMENDED_NEWS_SOURCES = [
  { name: '少数派', url: 'https://sspai.com/feed', category: '科技数码', weight: 5, fetchInterval: 60 },
  { name: 'Solidot', url: 'https://www.solidot.org/index.rss', category: '科技新闻', weight: 5, fetchInterval: 60 },
  { name: '阮一峰的网络日志', url: 'https://www.ruanyifeng.com/blog/atom.xml', category: '技术博客', weight: 5, fetchInterval: 120 },
  { name: 'V2EX', url: 'https://www.v2ex.com/index.xml', category: '技术社区', weight: 4, fetchInterval: 60 },
  { name: 'BBC中文', url: 'https://feeds.bbci.co.uk/zhongwen/simp/rss.xml', category: '国际新闻', weight: 4, fetchInterval: 120 }
]

// 第八中文网 CMS（第八中文网 / 新笔趣阁 同款结构），UTF-8、无需登录、无反爬验证码。
const d8zwRule = {
  name: '第八中文网',
  url: 'https://www.d8zw.com',
  charset: 'UTF-8',
  searchUrl: '/search.php?q={{key}}',
  searchList: 'div.box dl',
  searchName: 'dt a img',
  searchNameType: 'alt',
  searchAuthor: 'dd.book_other span',
  searchCover: 'dt a img',
  searchDetail: 'dt a',
  bookUrlPattern: '/(\\d+)/?$',
  detailName: 'h1',
  detailAuthor: '.options ul li a',
  detailCover: '.img-thumbnail',
  detailIntro: '#intro_pc',
  tocUrl: '{{bookUrl}}',
  catalogList: '.book_list2 ul li',
  catalogName: 'a',
  catalogUrl: 'a',
  contentUrl: '{{chapterUrl}}',
  bookContent: 'article',
  contentNextPage: 'a#next'
}

const xbiqugeRule = {
  ...d8zwRule,
  name: '新笔趣阁',
  url: 'https://www.xbiquge.info'
}

export const RECOMMENDED_BOOK_SOURCES = [d8zwRule, xbiqugeRule]
