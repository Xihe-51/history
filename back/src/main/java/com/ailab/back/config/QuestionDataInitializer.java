package com.ailab.back.config;

import com.ailab.back.Entity.Question;
import com.ailab.back.mapper.QuestionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 题库初始化器：应用启动时确保 question 表存在，并在题库为空时写入内置基础题目。
 * 这样趣味互动模块无需手工执行 SQL 即可直接答题（管理端也可继续维护题库）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionDataInitializer implements ApplicationRunner {

    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            questionMapper.initTable();
            Long existing = questionMapper.selectCount(null);
            if (existing != null && existing > 0) {
                log.info("题库已存在 {} 道题目，跳过初始化", existing);
                return;
            }
            List<Question> seeds = buildSeedQuestions();
            for (Question q : seeds) {
                questionMapper.insert(q);
            }
            log.info("题库初始化完成，写入 {} 道内置题目", seeds.size());
        } catch (Exception e) {
            log.warn("题库初始化失败（不影响主流程）：{}", e.getMessage());
        }
    }

    private Question build(String type, String topic, String content, List<String> options,
                           String answer, String analysis, String difficulty) {
        Question q = new Question();
        q.setQuestionType(type);
        q.setTopicType(topic);
        q.setContent(content);
        q.setAnswer(answer);
        q.setAnalysis(analysis);
        q.setDifficulty(difficulty);
        try {
            q.setOptions(objectMapper.writeValueAsString(options));
        } catch (Exception e) {
            q.setOptions("[]");
        }
        return q;
    }

    private static List<String> opts(String... values) {
        return new ArrayList<>(List.of(values));
    }

    private List<Question> buildSeedQuestions() {
        List<Question> list = new ArrayList<>();

        // ===== 朝代/通史 =====
        list.add(build("single", "dynasty", "中国历史上第一个大一统王朝是？",
                opts("秦朝", "汉朝", "商朝", "周朝"), "A",
                "公元前221年秦始皇统一六国，建立秦朝，是中国历史上第一个大一统王朝。", "easy"));
        list.add(build("single", "dynasty", "唐朝的开国皇帝是？",
                opts("李渊", "李世民", "杨坚", "赵匡胤"), "A",
                "公元618年李渊建立唐朝，是为唐高祖；李世民是第二位皇帝。", "easy"));
        list.add(build("single", "dynasty", "下列哪个朝代存在时间最长？",
                opts("周朝", "汉朝", "唐朝", "明朝"), "A",
                "周朝约公元前1046年至公元前256年，延续约八百年，是中国历史上最长的王朝。", "medium"));
        list.add(build("single", "dynasty", "元朝的建立者是？",
                opts("忽必烈", "成吉思汗", "铁木真", "窝阔台"), "A",
                "1271年忽必烈定国号为元，建立元朝；成吉思汗（铁木真）是蒙古帝国的奠基者。", "medium"));
        list.add(build("single", "dynasty", "三国时期“魏”的建立者是？",
                opts("曹丕", "曹操", "刘备", "孙权"), "A",
                "220年曹丕代汉称帝，国号魏；曹操生前未称帝，被追尊为魏武帝。", "medium"));
        list.add(build("judge", "dynasty", "秦朝是中国历史上第一个世袭制王朝。",
                opts("正确", "错误"), "B",
                "错误。中国历史上第一个世袭制王朝是夏朝，秦朝是第一个大一统王朝。", "medium"));
        list.add(build("judge", "dynasty", "隋朝开创了科举制度。",
                opts("正确", "错误"), "A",
                "正确。隋朝创立科举制，唐朝进一步完善，成为此后千余年的人才选拔制度。", "easy"));

        // ===== 历史事件 =====
        list.add(build("single", "event", "张骞出使西域发生在哪个朝代？",
                opts("西汉", "东汉", "唐朝", "明朝"), "A",
                "汉武帝时期张骞两次出使西域，开辟了丝绸之路。", "easy"));
        list.add(build("single", "event", "“赤壁之战”交战的主要双方是？",
                opts("曹操与孙刘联军", "秦与赵", "项羽与刘邦", "魏与蜀"), "A",
                "公元208年，曹操南下，孙刘联军在赤壁大败曹军，奠定三国鼎立格局。", "medium"));
        list.add(build("single", "event", "商鞅变法发生在哪个诸侯国？",
                opts("秦国", "齐国", "楚国", "赵国"), "A",
                "战国时期秦孝公任用商鞅变法，使秦国迅速强大。", "medium"));
        list.add(build("judge", "event", "“安史之乱”发生在唐朝。",
                opts("正确", "错误"), "A",
                "正确。755年安禄山、史思明发动叛乱，是唐朝由盛转衰的转折点。", "easy"));
        list.add(build("single", "event", "“陈桥兵变”促成了哪个王朝的建立？",
                opts("宋朝", "唐朝", "明朝", "元朝"), "A",
                "960年赵匡胤在陈桥驿发动兵变，黄袍加身，建立北宋。", "medium"));
        list.add(build("multiple", "event", "下列属于秦朝重要举措的有？（多选）",
                opts("统一文字", "统一货币", "修筑长城", "开创科举"), "A,B,C",
                "秦朝统一文字、货币、度量衡，并修筑长城；科举制由隋朝开创。", "hard"));
        list.add(build("single", "event", "“焚书坑儒”与哪位历史人物直接相关？",
                opts("秦始皇", "汉武帝", "唐太宗", "明太祖"), "A",
                "秦始皇为加强思想控制，采纳李斯建议焚书坑儒。", "easy"));

        // ===== 历史人物 =====
        list.add(build("single", "person", "“卧薪尝胆”讲述的是哪位国君的故事？",
                opts("越王勾践", "吴王夫差", "楚庄王", "齐桓公"), "A",
                "越王勾践战败后卧薪尝胆、发奋图强，最终灭吴报仇。", "easy"));
        list.add(build("single", "person", "孔子是哪个时期的人物？",
                opts("春秋时期", "战国时期", "秦朝", "汉朝"), "A",
                "孔子（前551—前479）生活于春秋末期，是儒家学派创始人。", "easy"));
        list.add(build("single", "person", "“完璧归赵”中的主角是？",
                opts("蔺相如", "廉颇", "荆轲", "商鞅"), "A",
                "蔺相如奉璧出使秦国，机智周旋使和氏璧完好归赵。", "medium"));

        // ===== 典故 =====
        list.add(build("single", "allusion", "成语“指鹿为马”与哪位人物有关？",
                opts("赵高", "李斯", "吕不韦", "蒙恬"), "A",
                "秦二世时赵高牵鹿上朝硬说成马，借以试探并清除异己。", "medium"));
        list.add(build("single", "allusion", "“破釜沉舟”出自哪场战役？",
                opts("巨鹿之战", "赤壁之战", "官渡之战", "长平之战"), "A",
                "项羽在巨鹿之战中破釜沉舟，大破秦军主力。", "medium"));
        list.add(build("single", "allusion", "“三顾茅庐”中，刘备三次拜访的是谁？",
                opts("诸葛亮", "庞统", "司马懿", "周瑜"), "A",
                "刘备三顾茅庐请诸葛亮出山，成就三分天下之基业。", "easy"));

        return list;
    }
}
