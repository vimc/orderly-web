import {mount} from "@vue/test-utils";
import ChangeLog from "../../../js/components/runReport/changeLog.vue";

describe(`changeLog`, () => {
    const changelogTypeOptions = ["internal", "public"]

    const changelogStyle = {
        label: {size: 2, justify: "text-right"},
        control: {size: 6}
    }
    const getWrapper = () => {
        return mount(ChangeLog,
            {
                propsData: {
                    changelogStyle: changelogStyle,
                    showChangelog: true,
                    changelogTypeOptions: changelogTypeOptions
                }
            })
    }

    it(`renders component as expected`, () => {
        const wrapper = getWrapper();
        expect(wrapper.find("#changelog-message").exists()).toBeTruthy()
        expect(wrapper.find("#changelog-type").exists()).toBeTruthy()
        expect(wrapper.vm.$data).toEqual({"changeLogMessageValue": "", "changeLogTypeValue": ""})
        expect(wrapper.vm.$props).toEqual(
            {
                changelogTypeOptions,
                changelogStyle,
                "showChangelog": true
            })
    })

    it(`does not render component if showChangeMessage is false`, async() => {
        const wrapper = getWrapper();
        await wrapper.setData({showChangelog: false})
        expect(wrapper.find("#changelog-message").exists()).toBeFalsy()
        expect(wrapper.find("#changelog-type").exists()).toBeFalsy()
    })

    it(`can set and get changelog message`, () => {
        const wrapper = getWrapper();
        const message = wrapper.find("#changelogMessage")
        message.setValue("new message")
        expect(wrapper.vm.$data.changeLogMessageValue).toBe("new message")

        expect(wrapper.emitted().changelogMessage.length).toBe(1)
        expect(wrapper.emitted().changelogMessage[0][0]).toEqual("new message")

        const messageValue = message.element as HTMLTextAreaElement
        expect(messageValue.value).toBe("new message")
    })

    it(`can select and get changelog type`, () => {
        const wrapper = getWrapper();
        const options = wrapper.find("#changelogType")
            .find("select").findAll("option")
        options.at(1).setSelected()

        expect(wrapper.emitted().changelogType.length).toBe(1)
        expect(wrapper.emitted().changelogType[0][0]).toEqual(changelogTypeOptions[1])

        expect(wrapper.vm.$data.changeLogTypeValue).toBe("public")
        const typeValue = wrapper.find("#changelogType").element as HTMLSelectElement
        expect(typeValue.value).toBe("public")
    })
})