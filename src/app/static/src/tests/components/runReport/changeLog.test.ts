import {mount} from "@vue/test-utils";
import ChangeLog from "../../../js/components/runReport/changeLog.vue";
import changeLog from "../../../js/components/runReport/changeLog.vue";

describe(`changeLog`, () => {
    const changelogTypeOptions = ["internal", "public"]

    const getWrapper = () => {
        return mount(ChangeLog,
            {
                propsData: {
                    changelogTypeOptions: changelogTypeOptions
                }
            })
    }

    it(`renders component as expected`, () => {
        const wrapper = getWrapper();
        expect(wrapper.find("#changelog-message").exists()).toBeTruthy()
        expect(wrapper.find("#changelog-type").exists()).toBeTruthy()
        expect(wrapper.vm.$data).toEqual({"changeLogMessageValue": "", "changeLogTypeValue": "internal"})
        expect(wrapper.vm.$props).toEqual(
            {
                changelogTypeOptions,
                "changelogStyleReport": false
            })
    })

    it(`emits changelogType first index when component gets mounted`, () => {
        const wrapper = getWrapper();
        expect(wrapper.emitted().changelogType.length).toBe(1)
        expect(wrapper.emitted().changelogType[0][0]).toBe("internal")
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

        expect(wrapper.emitted().changelogType.length).toBe(2)
        expect(wrapper.emitted().changelogType[0][0]).toEqual(changelogTypeOptions[0]) //on mount initial selection
        expect(wrapper.emitted().changelogType[1][0]).toEqual(changelogTypeOptions[1])  // selected value

        expect(wrapper.vm.$data.changeLogTypeValue).toBe("public")
        const typeValue = wrapper.find("#changelogType").element as HTMLSelectElement
        expect(typeValue.value).toBe("public")
    })

    it(`renders changelog col styles correctly if changelog-style-report prop is not set`, () => {
        const wrapper = getWrapper();

        const label = ["col-form-label", "col-sm-4", "text-left"]
        const control = ["col-sm-4"]

        const changelogMessage = wrapper.find(changeLog).find("#changelog-message")
        const changelogType= wrapper.find(changeLog).find("#changelog-type")

        expect(changelogMessage.find("label").classes()).toEqual(label)
        expect(changelogMessage.find("#change-message-control").classes()).toEqual(control)
        expect(changelogType.find("label").classes()).toEqual(label)
        expect(changelogType.find("#change-type-control").classes()).toEqual(control)
    })
})