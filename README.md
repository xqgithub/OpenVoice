
#Android编译说明

1、**local.properties**中添加

```bash
#线上环境地址
releaseHostUrl="releaseHostUrl"
#测试环境地址
debugHostUrl="debugHostUrl"
#预发布环境地址
foxHostUrl="foxHostUrl"

#release
releaseChainUrl="https://eth-mainnet.g.alchemy.com/v2/P0x3R0_YKTh8ajEAVv1ycuuDel3l5kyz"
#debug
debugChainUrl="https://eth-goerli.g.alchemy.com/v2/neU1HBR2jMhlOAUGmiZvIntwkxBBNCEB"

#参数使用实例
BuildConfig.HOST_URL
```

