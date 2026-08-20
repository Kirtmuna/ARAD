# ARAD(1.7.10)

[English](README.en.md) | [日本語](README.md)

[Minecraft](https://www.minecraft.net/ja-jp) | [forge1.7.10](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.7.10.html)

# このブランチはすずみやによる1.7.10移植版です。本家版(1.12.2)は当リポジトリのmainブランチにあります。

# 最新版のダウンロードは[こちら](https://github.com/ringo-1234/ARAD/releases/latest) から

## この Mod はなにか

ARAD は RealTrainMod(RTM),NGTLib,MCTE の非公式拡張物です。

## 注意事項

**当 Mod の使用による一切の責任を負いません。**
**KaizPatchX-1.10.1以降のバージョンが必須です。**
**本家RTMでも動作する可能性がありますが、互換性は検証されていません。**

**[PR] CrossTie-1.0.0-Alpha11以降のバージョンがあると、より良いパフォーマンスと経験が得られます。**
[CrossTieのDLはこちら](https://github.com/suzumiyatrainer/CrossTie)

## 導入方法

当 Mod を DL し、mods フォルダに入れてください

## 使用方法

まず、前提として、マップが利用できます、また、駅ブロック,制限ブロックの2つブロックが追加されます。
駅ブロックにAppleExtended内蔵のARTPEによる編成アイテムをセットし、マップから駅を順に選択して路線を作ることで最初に選択された駅ブロックの中に入っている編成アイテムの編成を呼び出し、終点駅まで順々に停車します。

## 知っておいたら便利

・このModはそれぞれの編成に毎tick処理を施すのでTPSの低下は体感で感じられる場合もあります。
・このModの処理はレールという概念を把握していないので、編成が別の方向にいってしまっても編成からは対応ができません。
(レールという概念を入れると先のレールを読み込む処理が出てきてしまうため大幅なTPS低下が見られるためです。)
・現段階では始点駅でスポーンし、終点駅でデスポーンするという処理です、気になる場合は始点終点を車庫にすることをお勧めします。

## 謝辞

公開にあたり、配布許可を頂いた[NGT-5479](https://twitter.com/ngt5479) 様に、この場をお借りして感謝申し上げます。
