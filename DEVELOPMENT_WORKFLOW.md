## Git Workflow

Git history 應盡可能呈現 TDD 的演進：

```text
RED
 ↓
GREEN
 ↓
REFACTOR
 ↓
下一個 RED
```

每次只處理一個 Baby Step。

Commit message 應讓人可以從 Git history 理解：

- 測試想表達什麼
- 為什麼 production code 出現
- 哪一次 refactor 改善了什麼

本專案從獨立的空白 root commit 開始，不把既有完成版的 Git history 混入 TDD 練習。

## Decision Rules

後續每一個 Baby Step 的判斷優先順序：

1. 新使用者行為 → E2E
2. 可以用最小實作 Green → 先 Green
3. 下一個增量會卡住設計 → 先 Refactor
4. E2E 難以承擔複雜度 → Unit Test
5. 需要隔離 collaborator → Test Double / Interaction Test
6. GOOS 有對應測試寫法 → 優先忠實採用

### Reference Decision

本專案使用不同 reference 來回答不同問題：

- GOOS 原書 → TDD、Outside-In、London School 與設計思想
- `titangene/goos-java` → Baby Step 與 TDD 演進的參考
- `letterliu/goos-java-practice/main` → 最終套件版本、工具、環境設定與實作方式的基準

其中 `letterliu/goos-java-practice/main` 的最終實作優先於自行推測或自行選擇的方案。

如果目前 Baby Step 需要加入套件、工具或環境設定，應先確認 `main` 的實際實作，再決定目前需要引入的最小部分。

不能因為某個工具或版本「看起來合理」就自行替換 `main` 的實作。

## Green 必須有實際證據

不能僅根據程式碼推測測試應該通過。

每個 Baby Step 在宣稱 Green 前，必須實際執行目前適用的測試或驗證指令，並以實際輸出作為 Green 的依據。

### Core Principle

> 需求驅動測試，測試驅動設計；Green 後保持設計健康；複雜度出現才引入 Unit Test。

## Current Progress

目前專案處於真正的空白起點。

目前唯一的 commit 是：

`chore: initialize empty project`

下一個預定 Baby Step：

`Hello World Stair-step Test`

而且：「在 Stair-step Test 通過之前，不開始建立 Auction Sniper 的完整 production architecture。」

## Local-first Workflow

在建立 commit 之前，先在 local repository 完成與確認修改。

基本流程：

```text
提出下一個 Baby Step
        ↓
說明目前狀況與設計決策
        ↓
在 local 建立 / 修改內容
        ↓
git status / git diff
        ↓
確認修改內容
        ↓
建立 commit
        ↓
push 到 GitHub
```

Remote repository 不作為第一次檢查修改內容的地方。

Commit 前應確認：

```text
修改
 ↓
git status
 ↓
git diff
 ↓
確認修改範圍
 ↓
git add
 ↓
git diff --cached
 ↓
確認 staged content
 ↓
建立 commit
 ↓
push
```

確認內容與預期一致後，才建立 commit。

### Decision Gate

每一個 Baby Step 開始前，先說明：

- 目前專案狀態
- 從前一步觀察到的變化
- 下一個 Baby Step 的目的
- 為什麼現在應該採取這個步驟
- 是否需要參考 `letterliu/goos-java-practice/main`
- 預計新增或修改哪些內容

在使用者確認前，不進入下一個實作決策。

確認後，由助手提供需要建立或修改的檔案內容與指令。

使用者在 local repository 建立修改後，再透過 `git status`、`git diff` 與 `git diff --cached` 確認內容。

只有確認修改內容符合預期後，才建立 commit 並 push。

## Development Environment

本專案目前從空白起點開始。

不預先建立完整的 Java、Build、Docker、CI/CD 或測試環境。

當後續 Baby Step 需要某項環境設定時，再由測試與實際需求逐步引入。

開發環境以使用者目前的 macOS、Docker Desktop 與 VS Code 為基礎。

如果需要環境設定或相容性資訊，必須先參考 `letterliu/goos-java-practice` 的 `main` 實際實作。

環境、工具與套件可以依照 Baby Step 的需求分階段引入，但不自行建立與 `main` 不同的最終方案。

在尚未查證 `main` 的具體設定之前，不自行猜測版本、工具或實作方式。
