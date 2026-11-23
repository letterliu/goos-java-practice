# Development Workflow

## Git Workflow

Git history 應盡可能呈現 TDD 的演進：

```text
DEFINE
  ↓
RED
  ↓
GREEN
  ↓
REFACTOR（必要時）
  ↓
下一個 DEFINE
```

每次只處理一個 Baby Step。

Commit message 應讓人可以從 Git history 理解：

- 測試想表達什麼。
- 為什麼需要新增 production code。
- 為什麼需要新增或調整測試基礎設施。
- 哪一次 refactor 改善了什麼。

本專案從獨立的空白 root commit 開始，不把既有完成版的 Git history 混入 TDD 練習。

## Decision Rules

後續每一個 Baby Step 的判斷優先順序：

1. 新使用者行為 → E2E Test
2. 可以用最小實作 Green → 先 Green
3. 下一個增量會卡住設計 → 先 Refactor
4. E2E 難以承擔複雜度 → Unit Test
5. 需要隔離 collaborator → Test Double / Interaction Test
6. GOOS 有對應測試寫法 → 優先忠實採用

### TDD Commit Boundaries

Git commit 應該呈現 TDD 的意圖與演進，而不是記錄每一次編譯狀態或每一個實作細節。

對於一個新的使用者可觀察行為，依照以下階段進行：

```text
DEFINE
  ↓
RED
  ↓
必要的 Infrastructure 補足與驗證
  ↓
可信賴的 Product RED
  ↓
GREEN
  ↓
必要時 REFACTOR
```

Infrastructure 的補足與驗證可以在 RED 診斷過程中反覆發生，不必預先完成所有環境建置。

#### 1. DEFINE

DEFINE 的目的是定義完整的使用者行為，以及進入 TDD 循環所需的最小測試結構。

第一個 commit 可以包含：

- 完整的使用者可觀察測試流程。
- 表達測試流程所需的最小 Test Fixture。
- 必要的 Stub、Collaborator 或其他測試結構。
- 讓測試程式可以編譯與執行所需的最小技術設定。

DEFINE 階段不要求加入真正的行為驗證，也不要求測試因產品行為缺失而失敗。

例如，E2E Scenario 可以先定義完整流程，而 Fake Server 與 Application Runner 只提供必要的結構及方法入口。

此時的目標是讓測試意圖清楚且具備可執行的基本結構，而不是完成產品行為。

#### 2. RED

RED 階段加入真正的行為驗證。

例如：

- 驗證 UI 顯示預期狀態。
- 驗證 Fake Server 收到預期訊息。
- 驗證使用者能觀察到預期的最終結果。

RED 不只是讓測試失敗，而是要取得能夠歸因於產品行為缺失的可信賴失敗。

測試第一次執行時，可能因第一個 Assertion 失敗而提前結束，導致後續 Assertions 尚未執行。這是正常的 fail-fast 行為，不代表完整測試意圖有問題。

因此，必須區分：

**完整測試意圖（Complete Test Intent）**

完整描述這個使用者行為所需要的所有步驟與驗證，即使第一次執行時無法到達所有 Assertions。

**可信賴的產品 RED（Trustworthy Product RED）**

測試環境與觀察路徑已經確認有效，測試因產品尚未實作要求的行為而失敗。

只有取得可信賴的產品 RED，才進入 GREEN。

#### 3. Infrastructure：依需求逐步補足

Infrastructure 不是必須預先完成的獨立階段，而是由測試實際揭露的需求所驅動。

當測試無法可靠地驗證行為時，先診斷失敗原因，再決定是否需要補足 Infrastructure。

失敗原因分為三種類型：

**Infrastructure Failure**

外部環境或工具本身未正常運作。

例如：

- XMPP Server 尚未啟動。
- XMPP Connection 被拒絕。
- 必要的測試依賴不存在。
- 測試執行環境無法正常啟動。

**Observation Failure**

測試觀察路徑無法可靠地取得預期資訊。

例如：

- Message Listener 尚未註冊。
- Listener 沒有將收到的 Message 放入 Queue。
- Fake Server 無法取得測試需要的訊息。
- UI Driver 無法正確觀察 Swing 元件。

**Product Behavior Failure**

Infrastructure 與觀察路徑都已確認正常，但產品沒有產生預期行為。

只有第三種情況才是可信賴的產品 RED。

Infrastructure 的處理原則：

1. 先確認目前失敗屬於哪一種類型。
2. 只補足目前測試所揭露的最小必要能力。
3. 適當驗證新增的 Infrastructure 是否真的有效。
4. 再次執行完整測試流程。
5. 重複上述步驟，直到取得可信賴的產品 RED。

Infrastructure 本身也需要適當的驗證。

例如：

- 使用另一個 XMPP Client 發送訊息，確認 Fake Server 的 Listener 與 Queue 能正常接收。
- 建立簡單的 Swing 測試視窗，確認 WindowLicker 能正確觀察 UI。

這些驗證的目的，是確認測試基礎設施可靠，不應與產品行為測試混淆。

Infrastructure 可以建立獨立 commit，也可以依照變更責任與實際需要，與相關測試工作一起提交。

不要求每一項 Infrastructure 都建立獨立 commit，也不預先承諾固定的 commit 數量。

#### 4. GREEN

GREEN 階段加入讓目前行為驗證通過所需的最小 Production Implementation。

GREEN 代表測試已經實際驗證所要求的行為並成功通過。

單純成功編譯不視為 GREEN。

即使 DEFINE 已經定義完整使用者流程，GREEN 仍然應該小步前進，一次只實作目前合理的行為增量。

不因為完整 E2E Scenario 已經定義，就一次實作所有尚未完成的行為。

#### 5. REFACTOR

GREEN 之後，只有當目前設計開始阻礙下一個增量、出現不必要的複雜度、重複或其他設計問題時，才進行 REFACTOR。

REFACTOR 必須維持既有行為驗證仍然通過。

### Commit Boundary Decision Rules

新增 class、編譯失敗或編譯成功，本身都不決定 commit 的類型。

應該根據這次變更的目的與責任決定 commit 邊界。

因此：

- DEFINE 用來建立完整測試意圖與最小測試結構。
- RED 用來加入真正的行為驗證，並取得可信賴的產品失敗。
- Infrastructure commit 用來建立或改善必要的測試環境與觀察能力。
- GREEN 用來實作目前要求的最小產品行為。
- REFACTOR 用來改善設計而不改變既有行為。

其他原則：

- 不為後續開發建立特殊的 `skeleton` commit 類型。
- 不因編譯從失敗變成成功，就單獨建立一個 commit。
- 不機械地按照 class、file 或 compiler 狀態建立 commit。
- 不預先建立尚未被測試需求揭露的 Infrastructure。
- 不為了減少 commit 數量而混合不同責任的變更。
- 也不為了增加 commit 數量而刻意拆分沒有獨立意義的修改。

Git history 應該優先呈現使用者行為，以及 DEFINE → RED → GREEN → REFACTOR 的演進。

## Reference Decision

本專案使用不同 reference 來回答不同問題：

- GOOS 原書 → TDD、Outside-In、London School 與設計思想。
- `titangene/goos-java` → Baby Step 與 TDD 演進的參考。
- `letterliu/goos-java-practice/main` → 最終套件版本、工具、環境設定與實作方式的基準。

其中 `letterliu/goos-java-practice/main` 的最終實作優先於自行推測或自行選擇的方案。

如果目前 Baby Step 需要加入套件、工具或環境設定，應先確認 `main` 的實際實作，再決定目前需要引入的最小部分。

不能因為某個工具或版本「看起來合理」就自行替換 `main` 的實作。

Titan Reference 是理解 Baby Step 演進的參考，不代表必須完全複製其 commit 邊界、Infrastructure 引入順序或所有實作細節。

當 Titan 的做法與本專案已經確認的設計原則不同時，以本專案的設計原則為準。

## Green Must Have Evidence

不能僅根據程式碼推測測試應該通過。

每個 Baby Step 在宣稱 Green 前，必須實際執行目前適用的測試或驗證指令，並以實際輸出作為 Green 的依據。

如果測試環境或觀察路徑尚未確認可靠，不得將測試結果直接解讀為產品行為的 RED 或 GREEN。

### Core Principle

> 需求驅動測試，測試驅動設計；依測試需求引入 Infrastructure；取得可信賴的 RED 後，以最小實作達成 GREEN；Green 後保持設計健康，複雜度出現才引入 Unit Test。

## Current Progress

目前專案已經進入 Auction Sniper 的 E2E TDD 練習。

已完成的重要階段：

- 建立獨立的空白 root commit。
- 建立 Docker Java 8 測試工具環境。
- 定義 Auction Sniper 的 E2E 使用者行為流程。

目前正在重新整理第一個 E2E RED Baby Step。

先前的 RED commit 已經在本地拆除，準備依照 DEFINE 與 RED 的新共識重新建立 Git history。

目前本地分支以 `0ad8363`（`chore(e2e): establish Docker test environment`）為基礎。

後續應依照目前的完整 E2E Scenario，先確認 DEFINE 所需的最小測試結構，再加入真正的行為驗證。

不得因為完整 E2E Scenario 已經定義，就提前建立尚未被測試需求揭露的 XMPP Infrastructure 或其他技術能力。

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
逐一確認修改內容
        ↓
git add
        ↓
git diff --cached
        ↓
確認 staged content
        ↓
建立 commit
        ↓
確認 local commit
        ↓
push 到 GitHub
```

Remote repository 不作為第一次檢查修改內容的地方。

Commit 前應確認：

1. 修改目的是否符合目前 Baby Step。
2. 是否混入其他責任的變更。
3. 是否有不必要的檔案或套件。
4. `git status` 是否符合預期。
5. `git diff` 是否符合預期。
6. `git diff --cached` 是否符合預期。

確認內容與預期一致後，才建立 commit。

Local commit 確認正確後，才更新 Remote repository。

不得未經確認就使用會覆蓋或改寫 Remote history 的 Git 操作。

## Decision Gate

每一個 Baby Step 開始前，先說明：

- 目前專案狀態。
- 從前一步觀察到的變化。
- 下一個 Baby Step 的目的。
- 為什麼現在應該採取這個步驟。
- 是否需要參考 `letterliu/goos-java-practice/main`。
- 預計新增或修改哪些內容。

在使用者確認前，不進入下一個實作決策。

確認後，由助手提供需要建立或修改的檔案內容與指令。

使用者在 local repository 建立修改後，再透過 `git status`、`git diff` 與 `git diff --cached` 確認內容。

檢查多個檔案時，應逐一檢查，不要一次要求使用者確認所有檔案。

只有確認修改內容符合預期後，才建立 commit 並 push。

每次只處理一個 Baby Step，一次只前進一個合理的小步驟。

使用者負責實際修改與執行測試，助手依照使用者回報的結果進行分析，再引導下一步。

不要一次透露多個後續 Baby Steps，也不要在使用者尚未確認前預先決定後續實作。

## Development Environment

本專案以使用者目前的 macOS、Docker Desktop 與 VS Code 為開發基礎。

環境與工具依照 Baby Step 的實際需求逐步引入。

不預先建立完整的 Java、Build、Docker、CI/CD 或測試環境。

當後續 Baby Step 需要某項環境設定時，再由測試與實際需求逐步引入。

目前已經建立 Java 8 與 Xvfb 的 Docker 測試工具環境。

該環境只提供 Java 編譯與測試執行所需的工具，不代表已經建立 Openfire、Docker Compose 或完整 XMPP 測試環境。

如果需要新增環境設定或相容性資訊，必須先參考 `letterliu/goos-java-practice` 的 `main` 實際實作。

環境、工具與套件可以依照 Baby Step 的需求分階段引入，但不自行建立與 `main` 不同的最終方案。

在尚未查證 `main` 的具體設定之前，不自行猜測版本、工具或實作方式。
